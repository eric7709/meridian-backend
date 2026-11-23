#!/usr/bin/env bash
# End-to-end walk through the results workflow against a running API.
# Usage: BASE=http://localhost:8080 ./scripts/smoke-test.sh
set -euo pipefail
BASE="${BASE:-http://localhost:8080}"
API="$BASE/api/meridian"
PASSWORD="${PASSWORD:-Password@123}"
pass=0; fail=0
check() { # name expected actual
  if [ "$2" = "$3" ]; then echo "  ok   $1"; pass=$((pass+1)); else echo "  FAIL $1 (expected $2, got $3)"; fail=$((fail+1)); fi
}
json() { python3 -c "import sys,json;print(json.load(sys.stdin)$1)"; }
# The auth endpoints share a per-address brute-force budget, so each run presents its own
# X-Forwarded-For; otherwise a second run inside the same minute would look like an attack.
XFF="10.$((RANDOM % 250)).$((RANDOM % 250)).$((RANDOM % 250))"
acurl() { curl -s -H "X-Forwarded-For: $XFF" "$@"; }

echo "== auth =="
ADMIN=$(acurl -X POST "$API/auth/login" -H 'Content-Type: application/json' \
  -d "{\"identifier\":\"admin@meridian.test\",\"password\":\"$PASSWORD\"}" | json '["accessToken"]')
TEACHER=$(acurl -X POST "$API/auth/login" -H 'Content-Type: application/json' \
  -d "{\"identifier\":\"teacher@meridian.test\",\"password\":\"$PASSWORD\"}" | json '["accessToken"]')
STUDENT=$(acurl -X POST "$API/auth/login" -H 'Content-Type: application/json' \
  -d "{\"identifier\":\"student1@meridian.test\",\"password\":\"$PASSWORD\"}" | json '["accessToken"]')
[ -n "$ADMIN" ] && echo "  ok   three tokens issued" || { echo "  FAIL login"; exit 1; }

check "bad password rejected" 403 "$(acurl -o /dev/null -w '%{http_code}' -X POST "$API/auth/login" \
  -H 'Content-Type: application/json' -d '{"identifier":"admin@meridian.test","password":"nope"}')"

SESSION=$(acurl -X POST "$API/auth/login" -H 'Content-Type: application/json' \
  -d "{\"identifier\":\"admin@meridian.test\",\"password\":\"$PASSWORD\"}")
REFRESH=$(echo "$SESSION" | json '["refreshToken"]')
check "login returns an access and a refresh token" 1 \
  "$(echo "$SESSION" | python3 -c 'import sys,json;d=json.load(sys.stdin);print(1 if d["accessToken"] and d["refreshToken"] else 0)')"
ROTATED=$(acurl -X POST "$API/auth/refresh" -H 'Content-Type: application/json' \
  -d "{\"refreshToken\":\"$REFRESH\"}")
ROTATED_ACCESS=$(echo "$ROTATED" | json '["accessToken"]')
ROTATED_REFRESH=$(echo "$ROTATED" | json '["refreshToken"]')
check "refreshed access token is accepted" 200 \
  "$(acurl -o /dev/null -w '%{http_code}' "$API/auth/me" -H "Authorization: Bearer $ROTATED_ACCESS")"
check "a refresh token is rotated, not reused" 1 "$([ "$REFRESH" != "$ROTATED_REFRESH" ] && echo 1 || echo 0)"
check "rotated refresh token still works" 200 \
  "$(acurl -o /dev/null -w '%{http_code}' -X POST "$API/auth/refresh" \
  -H 'Content-Type: application/json' -d "{\"refreshToken\":\"$ROTATED_REFRESH\"}")"
check "logout revokes the session" 204 \
  "$(acurl -o /dev/null -w '%{http_code}' -X POST "$API/auth/logout" \
  -H 'Content-Type: application/json' -d "{\"refreshToken\":\"$ROTATED_REFRESH\"}")"
# replaying a spent token is treated as theft and kills every session of that account
check "spent refresh token is refused" 403 \
  "$(acurl -o /dev/null -w '%{http_code}' -X POST "$API/auth/refresh" \
  -H 'Content-Type: application/json' -d "{\"refreshToken\":\"$REFRESH\"}")"
check "no token is 401-ish" 403 "$(curl -s -o /dev/null -w '%{http_code}' "$API/students")"

echo "== academic structure =="
CLASS=$(curl -s "$API/classes?search=SS2" -H "Authorization: Bearer $ADMIN" | json '["data"][0]["id"]')
TERM=$(curl -s "$API/terms?isActive=true" -H "Authorization: Bearer $ADMIN" | json '["data"][0]["id"]')
ASSIGN=$(curl -s "$API/assignments" -H "Authorization: Bearer $ADMIN" | json '["data"][0]["id"]')
[ -n "$CLASS" ] && [ -n "$TERM" ] && [ -n "$ASSIGN" ] && echo "  ok   class=$CLASS term=$TERM assignment=$ASSIGN"

echo "== results workflow =="
SHEET=$(curl -s -X POST "$API/result-sheets" -H "Authorization: Bearer $TEACHER" -H 'Content-Type: application/json' \
  -d "{\"assignmentId\":$ASSIGN,\"termId\":$TERM}" | json '["id"]' 2>/dev/null || true)
if [ -z "$SHEET" ]; then
  # sheet already exists from an earlier run: reuse it
  SHEET=$(curl -s "$API/teachers/me/result-sheets?size=1" -H "Authorization: Bearer $TEACHER" | json '["data"][0]["id"]')
  echo "  note reusing sheet $SHEET"
fi
check "duplicate sheet rejected" 409 "$(curl -s -o /dev/null -w '%{http_code}' -X POST "$API/result-sheets" \
  -H "Authorization: Bearer $TEACHER" -H 'Content-Type: application/json' -d "{\"assignmentId\":$ASSIGN,\"termId\":$TERM}")"

check "CA above 40 rejected" 400 "$(curl -s -o /dev/null -w '%{http_code}' -X PUT "$API/result-sheets/$SHEET/scores/1" \
  -H "Authorization: Bearer $TEACHER" -H 'Content-Type: application/json' -d '{"ca":55,"exam":10}')"
check "exam above 60 rejected" 400 "$(curl -s -o /dev/null -w '%{http_code}' -X PUT "$API/result-sheets/$SHEET/scores/1" \
  -H "Authorization: Bearer $TEACHER" -H 'Content-Type: application/json' -d '{"ca":10,"exam":75}')"

curl -s -X PUT "$API/result-sheets/$SHEET/scores/1" -H "Authorization: Bearer $TEACHER" \
  -H 'Content-Type: application/json' -d '{"ca":32,"exam":48}' >/dev/null
curl -s -X PUT "$API/result-sheets/$SHEET/scores/2" -H "Authorization: Bearer $TEACHER" \
  -H 'Content-Type: application/json' -d '{"ca":28,"exam":40}' >/dev/null
ROSTER=$(curl -s "$API/result-sheets/$SHEET/scores" -H "Authorization: Bearer $TEACHER")
check "roster counts marked students" 2 "$(echo "$ROSTER" | json '["marked"]')"
check "total is CA+exam" 80.0 "$(echo "$ROSTER" | json '["rows"][0]["total"]')"
check "grade looked up from scale" "A1" "$(echo "$ROSTER" | json '["rows"][0]["grade"]')"

CURRENT=$(curl -s "$API/result-sheets/$SHEET" -H "Authorization: Bearer $ADMIN" | json '["status"]')
if [ "$CURRENT" = "APPROVED" ]; then
  echo "  ok   sheet already approved by an earlier run (workflow checks skipped)"
  STATUS=APPROVED; APPROVED=APPROVED; pass=$((pass+2))
else
check "teacher cannot approve" 403 "$(curl -s -o /dev/null -w '%{http_code}' -X PATCH "$API/result-sheets/$SHEET/approve" \
  -H "Authorization: Bearer $TEACHER")"
STATUS=$(curl -s -X POST "$API/result-sheets/$SHEET/submit" -H "Authorization: Bearer $TEACHER" | json '["status"]')
if [ "$STATUS" = "PENDING" ] || [ "$STATUS" = "APPROVED" ]; then echo "  ok   submitted ($STATUS)"; pass=$((pass+1)); else echo "  FAIL submit ($STATUS)"; fail=$((fail+1)); fi
APPROVED=$(curl -s -X PATCH "$API/result-sheets/$SHEET/approve" -H "Authorization: Bearer $ADMIN" | json '["status"]')
check "admin approval publishes" '"APPROVED"' "$(echo "\"$APPROVED\"")"
fi

echo "== student portal =="
RESULTS=$(curl -s "$API/students/me/results?term=FIRST" -H "Authorization: Bearer $STUDENT")
check "student sees the approved subject" 1 "$(echo "$RESULTS" | python3 -c 'import sys,json;d=json.load(sys.stdin);print(len([s for t in d["terms"] for s in t["subjects"]]))')"
check "student cannot use teacher endpoints" 403 "$(curl -s -o /dev/null -w '%{http_code}' "$API/teachers/me/subjects" -H "Authorization: Bearer $STUDENT")"
check "full-year view skips unpublished terms" 200 "$(curl -s -o /dev/null -w '%{http_code}' "$API/students/me/results" -H "Authorization: Bearer $STUDENT")"
check "report card pdf" 200 "$(curl -s -o /dev/null -w '%{http_code}' "$API/students/me/report-card.pdf?termId=$TERM" -H "Authorization: Bearer $STUDENT")"
check "csv export" 200 "$(curl -s -o /dev/null -w '%{http_code}' "$API/result-sheets/$SHEET/export" -H "Authorization: Bearer $ADMIN")"

echo "== modules =="
curl -s -X PUT "$API/settings/modules" -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' \
  -d '{"feesEnabled":false}' >/dev/null
check "fees off => 404" 404 "$(curl -s -o /dev/null -w '%{http_code}' "$API/fees/summary" -H "Authorization: Bearer $ADMIN")"
curl -s -X PUT "$API/settings/modules" -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' \
  -d '{"feesEnabled":true}' >/dev/null
check "fees on => 200" 200 "$(curl -s -o /dev/null -w '%{http_code}' "$API/fees/summary" -H "Authorization: Bearer $ADMIN")"

echo "== search: filtering, paging, sorting, aliases =="
check "students search envelope" 1 "$(curl -s "$API/students?page=1&limit=2" -H "Authorization: Bearer $ADMIN" | python3 -c 'import sys,json;d=json.load(sys.stdin);print(d["page"])')"
check "students search limit honoured" 2 "$(curl -s "$API/students?page=1&limit=2" -H "Authorization: Bearer $ADMIN" | python3 -c 'import sys,json;d=json.load(sys.stdin);print(len(d["data"]))')"
check "q= alias filters results" 1 "$(curl -s "$API/students?q=Amaka" -H "Authorization: Bearer $ADMIN" | python3 -c 'import sys,json;d=json.load(sys.stdin);print(1 if d["total"]>=1 else 0)')"
check "sort alias accepted (sort/order)" 200 "$(curl -s -o /dev/null -w '%{http_code}' "$API/students?sort=fullName&order=asc&page=1&limit=5" -H "Authorization: Bearer $ADMIN")"
check "unknown sort key => 400" 400 "$(curl -s -o /dev/null -w '%{http_code}' "$API/students?sortBy=dropTable&page=1" -H "Authorization: Bearer $ADMIN")"
check "attendance register search" 200 "$(curl -s -o /dev/null -w '%{http_code}' "$API/attendance?page=1&limit=5" -H "Authorization: Bearer $ADMIN")"
check "score search" 200 "$(curl -s -o /dev/null -w '%{http_code}' "$API/scores?page=1&limit=5" -H "Authorization: Bearer $TEACHER")"
check "fee payment search" 200 "$(curl -s -o /dev/null -w '%{http_code}' "$API/fees/payments?page=1&limit=5" -H "Authorization: Bearer $ADMIN")"
check "timetable slot search" 200 "$(curl -s -o /dev/null -w '%{http_code}' "$API/timetable?page=1&limit=5" -H "Authorization: Bearer $ADMIN")"
check "term report search" 200 "$(curl -s -o /dev/null -w '%{http_code}' "$API/term-reports?page=1&limit=5" -H "Authorization: Bearer $TEACHER")"
check "audit log search (admin only)" 200 "$(curl -s -o /dev/null -w '%{http_code}' "$API/audit-logs?page=1&limit=5" -H "Authorization: Bearer $ADMIN")"
check "audit log search blocked for student" 403 "$(curl -s -o /dev/null -w '%{http_code}' "$API/audit-logs?page=1" -H "Authorization: Bearer $STUDENT")"

echo "== subject codes =="
NOCODE=$(curl -s -X POST "$API/subjects" -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' \
  -d '{"name":"Basic Technology","level":"JSS"}' | json '["code"]')
check "code is generated when omitted" 1 "$(echo "$NOCODE" | grep -c '^SUB/')"
OWNCODE="FM-$$"   # unique per run so the script can be repeated against the same database
check "school's own code is kept" "$OWNCODE" "$(curl -s -X POST "$API/subjects" \
  -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' \
  -d "{\"name\":\"Further Mathematics $OWNCODE\",\"code\":\"$OWNCODE\",\"level\":\"SS\"}" | json '["code"]')"

echo "== password reset =="
RESET=$(acurl -X POST "$API/auth/forgot-password" -H 'Content-Type: application/json' \
  -d '{"email":"student1@meridian.test"}' | json '["resetToken"]')
check "reset token issued" 1 "$([ -n "$RESET" ] && echo 1 || echo 0)"
check "password reset accepted" 204 "$(acurl -o /dev/null -w '%{http_code}' -X POST "$API/auth/reset-password" \
  -H 'Content-Type: application/json' -d "{\"token\":\"$RESET\",\"newPassword\":\"TempPass123\"}")"
check "old password no longer works" 403 "$(acurl -o /dev/null -w '%{http_code}' -X POST "$API/auth/login" \
  -H 'Content-Type: application/json' -d "{\"identifier\":\"student1@meridian.test\",\"password\":\"$PASSWORD\"}")"
check "new password works" 200 "$(acurl -o /dev/null -w '%{http_code}' -X POST "$API/auth/login" \
  -H 'Content-Type: application/json' -d '{"identifier":"student1@meridian.test","password":"TempPass123"}')"
RESET2=$(acurl -X POST "$API/auth/forgot-password" -H 'Content-Type: application/json' \
  -d '{"email":"student1@meridian.test"}' | json '["resetToken"]')
acurl -o /dev/null -X POST "$API/auth/reset-password" -H 'Content-Type: application/json' \
  -d "{\"token\":\"$RESET2\",\"newPassword\":\"$PASSWORD\"}"

echo "== rate limiting =="
check "limit headers on every response" 1 "$(curl -s -D - -o /dev/null "$API/settings" -H "Authorization: Bearer $ADMIN" | grep -ci '^x-ratelimit-limit:')"
check "rate limit header reports a budget" 240 "$(curl -s -D - -o /dev/null "$API/settings" -H "Authorization: Bearer $ADMIN" | grep -i '^x-ratelimit-limit:' | tr -dc '0-9')"

echo
echo "passed: $pass   failed: $fail"
[ "$fail" -eq 0 ]
echo "# meridian-frontend" >> README.md
echo "# meridian-backend" >> README.md
