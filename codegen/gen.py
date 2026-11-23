"""Generates the boring, identical 90% of every module from spec.py."""
import os
import shutil

from spec import ENTITIES, ENUM_PACKAGE, HAND_WRITTEN, DOMAIN_OF

ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "java", "com", "app", "meridian")
BASE = "com.app.meridian"

USER_SPEC = dict(
    name="User", domain="identity", table="users", path="users", plural="Users", crud=False,
    fields=[
        dict(name="identifier", type="String", req=True, uniq=True, search=True, filter=False, range=False, default=None, text=False, resp=True, request=True),
        dict(name="email", type="String", req=True, uniq=True, search=True, filter=False, range=False, default=None, text=False, resp=True, request=True),
        dict(name="passwordHash", type="String", req=True, uniq=False, search=False, filter=False, range=False, default=None, text=False, resp=False, request=False),
        dict(name="fullName", type="String", req=True, uniq=False, search=True, filter=False, range=False, default=None, text=False, resp=True, request=True),
        dict(name="role", type="Role", req=True, uniq=False, search=False, filter=True, range=False, default=None, text=False, resp=True, request=True),
        dict(name="active", type="Boolean", req=True, uniq=False, search=False, filter=True, range=False, default="true", text=False, resp=True, request=True),
        dict(name="lastLoginAt", type="LocalDateTime", req=False, uniq=False, search=False, filter=False, range=True, default=None, text=False, resp=True, request=True),
    ],
    relations=[], extras=[], unique_together=[], filters=[],
)

ALL = {e["name"]: e for e in ENTITIES}
ALL.setdefault("User", USER_SPEC)


def cap(s):
    return s[0].upper() + s[1:]


def var(s):
    return s[0].lower() + s[1:]


def field(spec, name):
    for x in spec["fields"]:
        if x["name"] == name:
            return x
    raise KeyError(name)


def req_name(rel):
    return rel.get("request_name") or rel["name"] + "Id"


def resp_name(rel):
    return rel.get("response_name") or rel["name"] + "Id"


def relation(spec, name):
    for x in spec["relations"]:
        if x["name"] == name:
            return x
    raise KeyError(name)


def path_type(spec, path):
    """Resolve a dotted mapping path (e.g. assignment.subject.name) to a Java type."""
    parts = path.split(".")
    cur = spec
    for i, part in enumerate(parts):
        if i == len(parts) - 1:
            return "Long" if part == "id" else field(cur, part)["type"]
        cur = ALL[relation(cur, part)["target"]]
    raise ValueError(path)


def is_enum(t):
    return t in ENUM_PACKAGE


def is_time(t):
    return t in ("LocalDate", "LocalDateTime", "LocalTime")


def java_imports(types, own_domain, extra=()):
    imps = set(extra)
    for t in types:
        if is_time(t):
            imps.add("java.time." + t)
        elif is_enum(t):
            pkg = ENUM_PACKAGE[t]
            imps.add(f"{BASE}.{pkg}.enums.{t}")
    return imps


def render_imports(imps, own_package=None):
    if own_package:
        imps = {i for i in imps if i.rsplit(".", 1)[0] != own_package}
    if not imps:
        return ""
    groups = sorted(imps, key=lambda x: (not x.startswith("java."), x))
    return "\n".join(f"import {i};" for i in groups) + "\n"


def write(rel_path, content):
    path = os.path.join(ROOT, rel_path)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as fh:
        fh.write(content)


def jstr(s):
    return '"' + s.replace('"', '\\"') + '"'


def label(spec):
    return spec["name"].replace("ClassRoom", "Class")


# --------------------------------------------------------------------------- entity
def gen_entity(spec):
    name, domain = spec["name"], spec["pkg"]
    lines = [f"package {BASE}.{domain};", ""]
    imps = {"jakarta.persistence.Column", "jakarta.persistence.Entity",
            "jakarta.persistence.Table", "lombok.Getter", "lombok.NoArgsConstructor",
            "lombok.Setter", f"{BASE}.common.BaseEntity"}
    types = [x["type"] for x in spec["fields"]]
    imps |= java_imports(types, domain)
    for rel in spec["relations"]:
        imps.add(f"{BASE}.{target_pkg(rel['target'])}.{rel['target']}")
        if rel["one_to_one"]:
            imps.add("jakarta.persistence.OneToOne")
        else:
            imps.add("jakarta.persistence.ManyToOne")
        imps.add("jakarta.persistence.FetchType")
        imps.add("jakarta.persistence.JoinColumn")
        imps.add("jakarta.persistence.ForeignKey")
        imps.add("org.hibernate.annotations.OnDelete")
        imps.add("org.hibernate.annotations.OnDeleteAction")
        if not rel["null"]:
            imps.add("jakarta.validation.constraints.NotNull")
    if any(x["req"] for x in spec["fields"]):
        imps.add("jakarta.validation.constraints.NotNull")
    if any(is_enum(x["type"]) for x in spec["fields"]):
        imps.add("jakarta.persistence.Enumerated")
        imps.add("jakarta.persistence.EnumType")
    if spec.get("unique_together"):
        imps.add("jakarta.persistence.UniqueConstraint")
    lines.append(render_imports(imps, f"{BASE}.{domain}"))

    table = spec["table"]
    constraints = []
    for group in spec.get("unique_together", []):
        cols = []
        for g in group:
            if any(rl["name"] == g for rl in spec["relations"]):
                cols.append(relation(spec, g)["col"])
            else:
                cols.append(snake(g))
        cname = "uk_" + table + "_" + "_".join(group)
        if len(cname) > 60:
            cname = "uk_" + table + "_" + "_".join(g[:3] for g in group)
        constraints.append(
            f'        @UniqueConstraint(name = {jstr(cname)}, columnNames = {{{", ".join(jstr(c) for c in cols)}}})')
    if constraints:
        table_ann = (f'@Table(name = {jstr(table)}, uniqueConstraints = {{\n'
                     + ",\n".join(constraints) + "})")
    else:
        table_ann = f'@Table(name = {jstr(table)})'

    lines += [
        "@Getter",
        "@Setter",
        "@Entity",
        "@NoArgsConstructor",
        table_ann,
        f"public class {name} extends BaseEntity {{",
        "",
    ]
    for x in spec["fields"]:
        if x["req"]:
            lines.append("    @NotNull")
        col = [f"name = {jstr(snake(x['name']))}", "nullable = " + ("false" if x["req"] else "true")]
        if x["uniq"]:
            col.append("unique = true")
        if x["text"]:
            col.append('columnDefinition = "TEXT"')
        lines.append("    @Column(" + ", ".join(col) + ")")
        if is_enum(x["type"]):
            lines.insert(len(lines) - 1, "    @Enumerated(EnumType.STRING)")
        lines.append(f"    private {x['type']} {x['name']};")
        lines.append("")
    for rel in spec["relations"]:
        t = rel["target"]
        if rel["one_to_one"]:
            lines.append(f"    @OneToOne(fetch = FetchType.EAGER, optional = {str(rel['null']).lower()})")
        else:
            lines.append("    @ManyToOne(fetch = FetchType.EAGER)")
        join = [f'name = {jstr(rel["col"])}', f"nullable = {str(rel['null']).lower()}",
                f'foreignKey = @ForeignKey(name = {jstr("fk_" + table + "_" + rel["name"])})']
        if rel["one_to_one"]:
            join.append("unique = true")
        lines.append("    @JoinColumn(" + ", ".join(join) + ")")
        lines.append(f"    @OnDelete(action = OnDeleteAction.{rel['ondelete']})")
        if not rel["null"]:
            lines.append("    @NotNull")
        lines.append(f"    private {t} {rel['name']};")
        lines.append("")
    lines.append("}")
    write(f"{domain}/{name}.java", "\n".join(lines) + "\n")


def snake(s):
    out = []
    for i, ch in enumerate(s):
        if ch.isupper() and i:
            out.append("_")
        out.append(ch.lower())
    return "".join(out)


# ----------------------------------------------------------------------- repository
def gen_repository(spec):
    name, domain = spec["name"], spec["pkg"]
    lines = [f"package {BASE}.{domain};", ""]
    imps = {"org.springframework.data.jpa.repository.JpaRepository",
            "org.springframework.data.jpa.repository.JpaSpecificationExecutor"}
    methods = []
    for x in spec["fields"]:
        if not x["uniq"]:
            continue
        if x["type"] == "String":
            imps.add("java.util.Optional")
            methods.append(f"    boolean existsBy{cap(x['name'])}IgnoreCase(String value);")
            methods.append(f"    boolean existsBy{cap(x['name'])}IgnoreCaseAndIdNot(String value, Long id);")
            methods.append(f"    Optional<{name}> findBy{cap(x['name'])}IgnoreCase(String value);")
        else:
            methods.append(f"    boolean existsBy{cap(x['name'])}({x['type']} value);")
    for group in spec.get("unique_together", []):
        params, names = [], []
        for g in group:
            if any(rl["name"] == g for rl in spec["relations"]):
                params.append("Long " + g + "Id")
                names.append(cap(g) + "Id")
            else:
                fld = field(spec, g)
                params.append(f"{fld['type']} {g}")
                names.append(cap(g))
        methods.append(f"    boolean existsBy{'And'.join(names)}({', '.join(params)});")
    needs_opt = any(x["uniq"] and x["type"] == "String" for x in spec["fields"])
    if not needs_opt:
        imps.discard("java.util.Optional")
    param_types = [x["type"] for x in spec["fields"] if x["uniq"]]
    for group in spec.get("unique_together", []):
        for g in group:
            if not any(rl["name"] == g for rl in spec["relations"]):
                param_types.append(field(spec, g)["type"])
    imps |= java_imports(param_types, domain)
    imps |= {"java.util.List", "java.util.Optional", "java.util.Collection"}
    # enum constants used by the hand-declared query methods
    imps |= {f"{BASE}.{pkg}.enums.{e}" for e, pkg in ENUM_PACKAGE.items() if pkg == domain}
    lines.append(render_imports(imps))
    lines.append(f"public interface {name}Repository extends JpaRepository<{name}, Long>, "
                 f"JpaSpecificationExecutor<{name}> {{")
    lines.append("")
    methods.extend("    " + m for m in spec.get("repo_methods", []))
    if methods:
        lines.extend(methods)
        lines.append("")
    lines.append("}")
    write(f"{domain}/{name}Repository.java", "\n".join(lines) + "\n")


# ----------------------------------------------------------------------------- DTOs
def response_fields(spec):
    out = [("id", "Long"), ("createdAt", "LocalDateTime"), ("updatedAt", "LocalDateTime")]
    seen = {n for n, _ in out}
    for x in spec["fields"]:
        if x["resp"] and x["name"] not in seen:
            out.append((x["name"], x["type"]))
            seen.add(x["name"])
    for rel in spec["relations"]:
        if resp_name(rel) not in seen:
            out.append((resp_name(rel), "Long"))
            seen.add(resp_name(rel))
        if rel["display"] and rel["name"] + cap(rel["display"]) not in seen:
            out.append((rel["name"] + cap(rel["display"]), path_type(spec, rel["name"] + "." + rel["display"])))
            seen.add(rel["name"] + cap(rel["display"]))
    for acc, path in spec.get("extras", []):
        if acc not in seen:
            out.append((acc, path_type(spec, path)))
            seen.add(acc)
    return out


def gen_dtos(spec):
    name, domain = spec["name"], spec["pkg"]
    pkg = f"{domain}/dto"
    # request
    fields = [(x["name"], x["type"]) for x in spec["fields"] if x["request"] and x["name"] != "total"]
    fields += [(req_name(rel), "Long") for rel in spec["relations"]]
    imps = java_imports([t for _, t in fields], domain)
    body = ", ".join(f"{t} {n}" for n, t in fields)
    write(f"{pkg}/{name}RequestDTO.java",
          f"package {BASE}.{domain}.dto;\n\n{render_imports(imps)}"
          f"/** Payload accepted by create and update. Update only applies non-null fields. */\n"
          f"public record {name}RequestDTO({body}) {{\n}}\n")

    # response
    rf = response_fields(spec)
    imps = java_imports([t for _, t in rf], domain, extra={
        "lombok.AllArgsConstructor", "lombok.Getter", "lombok.NoArgsConstructor", "lombok.Setter"})
    lines = [f"package {BASE}.{domain}.dto;", "", render_imports(imps),
             "@Getter", "@Setter", "@AllArgsConstructor", "@NoArgsConstructor",
             f"public class {name}ResponseDTO {{", ""]
    for n, t in rf:
        lines.append(f"    private {t} {n};")
    lines += ["}", ""]
    write(f"{pkg}/{name}ResponseDTO.java", "\n".join(lines))

    # criteria
    crit = [("String", "search")]
    for rel in spec["relations"]:
        if rel["filter"]:
            crit.append(("Long", rel["name"] + "Id"))
    for x in spec["fields"]:
        if x["filter"]:
            crit.append((x["type"], x["name"]))
    for acc, t, _ in spec.get("filters", []):
        if (t, acc) not in crit:
            crit.append((t, acc))
    for x in spec["fields"]:
        if x["range"]:
            crit.append((x["type"], "min" + cap(x["name"])))
            crit.append((x["type"], "max" + cap(x["name"])))
    imps = java_imports([t for t, _ in crit], domain, extra={
        "lombok.Getter", "lombok.NoArgsConstructor", "lombok.Setter", "java.util.Set"})
    lines = [f"package {BASE}.{domain}.dto;", "", render_imports(imps),
             "/** Query parameters for listing: free text, filters, ranges, paging, sorting. */",
             "@Getter", "@Setter", "@NoArgsConstructor",
             f"public class {name}SearchCriteria {{", ""]
    for t, n in crit:
        lines.append(f"    private {t} {n};")
    lines += ["",
              "    private int page = 1;",
              "    private int limit = 20;",
              '    private String sortBy = "id";',
              '    private String sortDirection = "desc";',
              "",
              "    /** Aliases so the frontend can send q / sort / order exactly as the screens do. */",
              "    public void setQ(String q) {",
              "        this.search = q;",
              "    }",
              "",
              "    public void setSort(String sort) {",
              "        this.sortBy = sort;",
              "    }",
              "",
              "    public void setOrder(String order) {",
              "        this.sortDirection = order;",
              "    }",
              "}", ""]
    write(f"{pkg}/{name}SearchCriteria.java", "\n".join(lines))


# ---------------------------------------------------------------------------- mapper
def gen_mapper(spec):
    name, domain = spec["name"], spec["pkg"]
    imps = {"org.mapstruct.Mapper", "org.mapstruct.Mapping", "java.util.List",
            f"{BASE}.{domain}.dto.{name}ResponseDTO", f"{BASE}.{domain}.{name}"}
    lines = [f"package {BASE}.{domain}.utils;", "", render_imports(imps),
             '@Mapper(componentModel = "spring")',
             f"public interface {name}Mapper {{", ""]
    mappings = {}
    for rel in spec["relations"]:
        mappings[resp_name(rel)] = rel["name"] + ".id"
    for rel in spec["relations"]:
        if rel["display"]:
            mappings[rel["name"] + cap(rel["display"])] = rel["name"] + "." + rel["display"]
    for acc, path in spec.get("extras", []):
        mappings.setdefault(acc, path)
    for target, source in mappings.items():
        lines.append(f'    @Mapping(target = "{target}", source = "{source}")')
    lines.append(f"    {name}ResponseDTO toResponse({name} entity);")
    lines.append("")
    lines.append(f"    List<{name}ResponseDTO> mapAllToResponse(List<{name}> entities);")
    lines.append("}")
    write(f"{domain}/utils/{name}Mapper.java", "\n".join(lines) + "\n")


# ---------------------------------------------------------------------------- events
def gen_events(spec):
    name, domain = spec["name"], spec["pkg"]
    topic = spec["path"]
    imps = {"lombok.RequiredArgsConstructor", "org.springframework.messaging.simp.SimpMessagingTemplate",
            "org.springframework.stereotype.Component", f"{BASE}.{domain}.dto.{name}ResponseDTO"}
    lines = [f"package {BASE}.{domain};", "", render_imports(imps),
             "/** Pushes created / updated / deleted events to STOMP subscribers. */",
             "@Component", "@RequiredArgsConstructor", f"public class {name}Events {{", "",
             "    private final SimpMessagingTemplate messagingTemplate;", "",
             f"    public void broadcastCreated({name}ResponseDTO payload) {{",
             "        if (payload == null) {", "            return;", "        }",
             f'        messagingTemplate.convertAndSend("/topic/{topic}/created", payload);',
             "    }", "",
             f"    public void broadcastUpdated({name}ResponseDTO payload) {{",
             "        if (payload == null) {", "            return;", "        }",
             f'        messagingTemplate.convertAndSend("/topic/{topic}/updated", payload);',
             "    }", "",
             "    public void broadcastDeleted(Long id) {",
             "        if (id == null) {", "            return;", "        }",
             f'        messagingTemplate.convertAndSend("/topic/{topic}/deleted", id);',
             "    }", "}", ""]
    write(f"{domain}/{name}Events.java", "\n".join(lines) + "\n")


# -------------------------------------------------------------------------- services
def req_checks(spec):
    out = []
    for x in spec["fields"]:
        if not (x["request"] and x["req"]):
            continue
        if x["default"] or (x.get("autocode")):
            continue
        if x["type"] == "String":
            out.append(f'        ValidationUtils.requireNonBlank(request.{x["name"]}(), {jstr(x["name"])});')
        else:
            out.append(f'        ValidationUtils.requireNonNull(request.{x["name"]}(), {jstr(x["name"])});')
    for rel in spec["relations"]:
        if not rel["null"]:
            out.append(f'        ValidationUtils.requireNonNull(request.{req_name(rel)}(), {jstr(rel["name"])});')
    return out


def qualified_default(x):
    d = x["default"]
    return f"{x['type']}.{d}" if is_enum(x["type"]) else d


def scalar_setters(spec, autocode_field=None):
    out = []
    for x in spec["fields"]:
        if not x["request"]:
            continue
        n, t = x["name"], x["type"]
        if x.get("autocode_field") or x["name"] == autocode_field:
            continue
        if x["req"] and x["default"]:
            out.append(f"        entity.set{cap(n)}(request.{n}() == null ? {qualified_default(x)} : request.{n}());")
        elif x["req"]:
            if t == "String":
                out.append(f"        entity.set{cap(n)}(request.{n}().trim());")
            else:
                out.append(f"        entity.set{cap(n)}(request.{n}());")
        else:
            if t == "String":
                out.append(f"        entity.set{cap(n)}(ValidationUtils.trimOrNull(request.{n}()));")
            else:
                out.append(f"        if (request.{n}() != null) {{")
                out.append(f"            entity.set{cap(n)}(request.{n}());")
                out.append("        }")
    return out


def cache_evict_annotation(spec):
    caches = spec.get("cache_evict") or []
    if not caches:
        return []
    names = ", ".join(f'"{c}"' for c in caches)
    return [f"    @CacheEvict(cacheNames = {{{names}}}, allEntries = true)"]


def gen_create(spec):
    name, domain = spec["name"], spec["pkg"]
    v = var(name)
    types = [x["type"] for x in spec["fields"] if x["request"]]
    imps = {"org.springframework.stereotype.Service", "org.springframework.transaction.annotation.Transactional",
            f"{BASE}.{domain}.{name}", f"{BASE}.{domain}.{name}Events",
            f"{BASE}.{domain}.{name}Repository", f"{BASE}.{domain}.dto.{name}RequestDTO",
            f"{BASE}.{domain}.utils.{name}Mapper", f"{BASE}.exceptions.ConflictException",
            f"{BASE}.utils.ValidationUtils", "lombok.RequiredArgsConstructor"}
    imps |= java_imports(types, domain)
    if spec.get("cache_evict"):
        imps.add("org.springframework.cache.annotation.CacheEvict")
    if spec["relations"]:
        imps.add(f"{BASE}.utils.EntityResolver")
        for rel in spec["relations"]:
            imps.add(f"{BASE}.{target_pkg(rel['target'])}.{rel['target']}")
    autocode = spec.get("autocode")
    if autocode:
        imps.add("java.time.Year")

    lines = [f"package {BASE}.{domain}.services;", "", render_imports(imps),
             "@Service", "@RequiredArgsConstructor", f"public class Create{name} {{", ""]
    if spec["relations"]:
        lines.append("    private final EntityResolver entityResolver;")
    lines += [f"    private final {name}Repository {v}Repository;",
              f"    private final {name}Events {v}Events;",
              f"    private final {name}Mapper {v}Mapper;", "",
              "    @Transactional",
             *cache_evict_annotation(spec),
             f"    public {name} create({name}RequestDTO request) {{",
              '        ValidationUtils.requireNonNull(request, "Request body");']
    lines += req_checks(spec)
    lines.append("")
    for rel in spec["relations"]:
        lines.append(f"        {rel['target']} {rel['name']} = "
                     f"entityResolver.resolve{rel['target']}(request.{req_name(rel)}());")
    if spec["relations"]:
        lines.append("")
    # uniqueness
    for x in spec["fields"]:
        if not x["uniq"]:
            continue
        n = x["name"]
        if x["type"] == "String":
            lines += [
                f"        if (request.{n}() != null && {v}Repository.existsBy{cap(n)}IgnoreCase(request.{n}().trim())) {{",
                f'            throw new ConflictException({jstr(label(spec) + " with this " + n + " already exists")});',
                "        }",
            ]
        else:
            lines += [
                f"        if (request.{n}() != null && {v}Repository.existsBy{cap(n)}(request.{n}())) {{",
                f'            throw new ConflictException({jstr(label(spec) + " with this " + n + " already exists")});',
                "        }",
            ]
    for group in spec.get("unique_together", []):
        args, names = [], []
        for g in group:
            if any(rl["name"] == g for rl in spec["relations"]):
                args.append(f"request.{req_name(relation(spec, g))}()")
                names.append(cap(g) + "Id")
            else:
                args.append(f"request.{g}()")
                names.append(cap(g))
        lines += [f"        if ({v}Repository.existsBy{'And'.join(names)}({', '.join(args)})) {{",
                  f'            throw new ConflictException({jstr(label(spec) + " already exists for that combination")});',
                  "        }"]
    lines.append("")
    lines.append(f"        {name} entity = new {name}();")
    if autocode:
        field_name, prefix = autocode
        lines += [f"        String {field_name} = request.{field_name}() == null || request.{field_name}().isBlank()",
                  f"                ? next{cap(field_name)}()",
                  f"                : request.{field_name}().trim();",
                  f"        entity.set{cap(field_name)}({field_name});"]
    lines += scalar_setters(spec, autocode[0] if autocode else None)
    for rel in spec["relations"]:
        lines.append(f"        entity.set{cap(rel['name'])}({rel['name']});")
    lines += ["",
              f"        {name} saved = {v}Repository.save(entity);",
              f"        {v}Events.broadcastCreated({v}Mapper.toResponse(saved));",
              "        return saved;",
              "    }"]
    if autocode:
        field_name, prefix = autocode
        lines += ["",
                  f"    private String next{cap(field_name)}() {{",
                  f"        long sequence = {v}Repository.count() + 1;",
                  f"        String candidate;",
                  "        do {",
                  f'            candidate = "{prefix}/%d/%03d".formatted(Year.now().getValue(), sequence++);',
                  f"        }} while ({v}Repository.existsBy{cap(field_name)}IgnoreCase(candidate));",
                  "        return candidate;",
                  "    }"]
    lines += ["}", ""]
    write(f"{domain}/services/Create{name}.java", "\n".join(lines))


def gen_update(spec):
    name, domain = spec["name"], spec["pkg"]
    v = var(name)
    types = [x["type"] for x in spec["fields"] if x["request"]]
    imps = {"org.springframework.stereotype.Service", "org.springframework.transaction.annotation.Transactional",
            f"{BASE}.{domain}.{name}", f"{BASE}.{domain}.{name}Events",
            f"{BASE}.{domain}.{name}Repository", f"{BASE}.{domain}.dto.{name}RequestDTO",
            f"{BASE}.{domain}.utils.{name}Mapper", f"{BASE}.exceptions.ConflictException",
            f"{BASE}.exceptions.NotFoundException", f"{BASE}.utils.ValidationUtils",
            "lombok.RequiredArgsConstructor"}
    if spec.get("cache_evict"):
        imps.add("org.springframework.cache.annotation.CacheEvict")
    imps |= java_imports(types, domain)
    if spec.get("cache_evict"):
        imps.add("org.springframework.cache.annotation.CacheEvict")
    if spec["relations"]:
        imps.add(f"{BASE}.utils.EntityResolver")
        for rel in spec["relations"]:
            imps.add(f"{BASE}.{target_pkg(rel['target'])}.{rel['target']}")

    lines = [f"package {BASE}.{domain}.services;", "", render_imports(imps),
             "/** Applies only the fields present in the request; everything else is left alone. */",
             "@Service", "@RequiredArgsConstructor", f"public class Update{name} {{", ""]
    if spec["relations"]:
        lines.append("    private final EntityResolver entityResolver;")
    lines += [f"    private final {name}Repository {v}Repository;",
              f"    private final {name}Events {v}Events;",
              f"    private final {name}Mapper {v}Mapper;", "",
              "    @Transactional",
              *cache_evict_annotation(spec),
              f"    public {name} update(Long id, {name}RequestDTO request) {{",
              f'        ValidationUtils.requireNonNull(id, "{label(spec)} ID");',
              '        ValidationUtils.requireNonNull(request, "Request body");',
              f"        {name} entity = {v}Repository.findById(id)",
              f'                .orElseThrow(() -> new NotFoundException({jstr(label(spec) + " not found")}));']
    for rel in spec["relations"]:
        lines += [f"        if (request.{req_name(rel)}() != null) {{",
                  f"            entity.set{cap(rel['name'])}(entityResolver.resolve{rel['target']}(request.{req_name(rel)}()));",
                  "        }"]
    if spec["relations"]:
        lines.append("")
    for x in spec["fields"]:
        if not x["request"]:
            continue
        n, t = x["name"], x["type"]
        if t == "String":
            lines.append(f"        if (request.{n}() != null && !request.{n}().isBlank()) {{")
            if x["uniq"]:
                lines += [f"            if ({v}Repository.existsBy{cap(n)}IgnoreCaseAndIdNot(request.{n}().trim(), id)) {{",
                          f'                throw new ConflictException({jstr(label(spec) + " with this " + n + " already exists")});',
                          "            }"]
            lines += [f"            entity.set{cap(n)}(request.{n}().trim());", "        }"]
        else:
            lines += [f"        if (request.{n}() != null) {{",
                      f"            entity.set{cap(n)}(request.{n}());",
                      "        }"]
    lines += ["",
              f"        {name} saved = {v}Repository.save(entity);",
              f"        {v}Events.broadcastUpdated({v}Mapper.toResponse(saved));",
              "        return saved;",
              "    }", "}", ""]
    write(f"{domain}/services/Update{name}.java", "\n".join(lines))


def gen_delete(spec):
    name, domain = spec["name"], spec["pkg"]
    v = var(name)
    imps = {"org.springframework.stereotype.Service", "org.springframework.transaction.annotation.Transactional",
            f"{BASE}.{domain}.{name}Events", f"{BASE}.{domain}.{name}Repository",
            f"{BASE}.exceptions.NotFoundException", f"{BASE}.utils.ValidationUtils",
            "lombok.RequiredArgsConstructor"}
    if spec.get("cache_evict"):
        imps.add("org.springframework.cache.annotation.CacheEvict")
    lines = [f"package {BASE}.{domain}.services;", "", render_imports(imps),
             "@Service", "@RequiredArgsConstructor", f"public class Delete{name} {{", "",
             f"    private final {name}Repository {v}Repository;",
             f"    private final {name}Events {v}Events;", "",
             "    @Transactional",
             *cache_evict_annotation(spec),
             "    public void delete(Long id) {",
             f"        ValidationUtils.requireNonNull(id, \"{label(spec)} ID\");",
             f"        if (!{v}Repository.existsById(id)) {{",
             f'            throw new NotFoundException({jstr(label(spec) + " not found")});',
             "        }",
             f"        {v}Repository.deleteById(id);",
             f"        {v}Events.broadcastDeleted(id);",
             "    }", "}", ""]
    write(f"{domain}/services/Delete{name}.java", "\n".join(lines))


def gen_get(spec):
    name, domain = spec["name"], spec["pkg"]
    v = var(name)
    imps = {"org.springframework.stereotype.Service", "org.springframework.transaction.annotation.Transactional",
            f"{BASE}.{domain}.{name}", f"{BASE}.{domain}.{name}Repository",
            f"{BASE}.exceptions.NotFoundException", f"{BASE}.utils.ValidationUtils",
            "lombok.RequiredArgsConstructor"}
    lines = [f"package {BASE}.{domain}.services;", "", render_imports(imps),
             "@Service", "@RequiredArgsConstructor", f"public class Get{name}ById {{", "",
             f"    private final {name}Repository {v}Repository;", "",
             "    @Transactional(readOnly = true)", f"    public {name} get(Long id) {{",
             f"        ValidationUtils.requireNonNull(id, \"{label(spec)} ID\");",
             f"        return {v}Repository.findById(id)",
             f'                .orElseThrow(() -> new NotFoundException({jstr(label(spec) + " not found")}));',
             "    }", "}", ""]
    write(f"{domain}/services/Get{name}ById.java", "\n".join(lines))


def gen_search(spec):
    name, domain, plural = spec["name"], spec["pkg"], spec["plural"]
    v = var(name)
    sortable = ["id", "createdAt", "updatedAt"] + [x["name"] for x in spec["fields"]]
    imps = {"java.util.ArrayList", "java.util.List", "java.util.Map", "java.util.Set", "java.util.TreeSet",
            f"{BASE}.exceptions.BadRequestException",
            "jakarta.persistence.criteria.Path", "jakarta.persistence.criteria.Predicate",
            "org.springframework.data.domain.Page", "org.springframework.data.domain.PageRequest",
            "org.springframework.data.domain.Pageable", "org.springframework.data.domain.Sort",
            "org.springframework.data.jpa.domain.Specification",
            "org.springframework.stereotype.Service", "org.springframework.transaction.annotation.Transactional",
            f"{BASE}.{domain}.{name}", f"{BASE}.{domain}.{name}Repository",
            f"{BASE}.{domain}.dto.{name}SearchCriteria", "lombok.RequiredArgsConstructor"}
    imps |= java_imports([x["type"] for x in spec["fields"] if x["filter"] or x["range"]], domain)

    def getter(t, n):
        return f"criteria.get{cap(n)}()"

    lines = [f"package {BASE}.{domain}.services;", "", render_imports(imps),
             "/** Page-and-filter listing shared by every list screen. */",
             "@Service", "@RequiredArgsConstructor", f"public class Search{plural} {{", "",
             f"    private static final Set<String> SORTABLE = Set.of({', '.join(jstr(s) for s in sortable)});",
             "    /** Sortable paths that live on a related row, ordered inside the query. */",
             f"    private static final Set<String> RELATED_SORTABLE = Set.of({', '.join(jstr(s) for s in (spec.get('related_search') or []) + (spec.get('related_sort') or []))});",
             "    /** The frontend sorts by the leaf name (fullName), which maps to user.fullName. */",
             f"    private static final Map<String, String> RELATED_SORT_ALIASES = Map.of({', '.join(f'{jstr(p.split(chr(46))[-1])}, {jstr(p)}' for p in (spec.get('related_search') or []) + (spec.get('related_sort') or []))});",
             "",
             f"    private final {name}Repository {v}Repository;", "",
             "    @Transactional(readOnly = true)",
             f"    public Page<{name}> search({name}SearchCriteria criteria) {{",
             "        Sort.Direction direction = \"asc\".equalsIgnoreCase(criteria.getSortDirection())",
             "                ? Sort.Direction.ASC : Sort.Direction.DESC;",
             "        String sortBy = resolveSort(criteria.getSortBy());",
             "        int size = Math.min(Math.max(criteria.getLimit(), 1), 200);",
             "        int index = Math.max(criteria.getPage() - 1, 0);",
             "        Pageable pageable = RELATED_SORTABLE.contains(sortBy)",
             "                ? PageRequest.of(index, size)",
             "                : PageRequest.of(index, size, Sort.by(direction, sortBy));",
             f"        return {v}Repository.findAll(matching(criteria, sortBy, direction), pageable);",
             "    }", "",
             "    /** A typo in sortBy is a client bug: say so instead of silently sorting by id. */",
             "    private String resolveSort(String requested) {",
             '        if (requested == null || requested.isBlank()) {',
             '            return "id";',
             "        }",
             "        if (!SORTABLE.contains(requested) && !RELATED_SORTABLE.contains(requested)",
             "                && !RELATED_SORT_ALIASES.containsKey(requested)) {",
             '            throw new BadRequestException("Unknown sort field \'" + requested',
             '                    + "\'. Sortable: " + new TreeSet<>(SORTABLE) + " or "',
             '                    + new TreeSet<>(RELATED_SORT_ALIASES.keySet()));',
             "        }",
             "        return RELATED_SORT_ALIASES.getOrDefault(requested, requested);",
             "    }", "",
             f"    private Specification<{name}> matching({name}SearchCriteria criteria, String sortBy,",
             "            Sort.Direction direction) {",
             "        return (root, query, cb) -> {",
             "            List<Predicate> predicates = new ArrayList<>();"]
    searchables = [x for x in spec["fields"] if x["search"]]
    related = spec.get("related_search") or spec.get("search_related") or []
    terms = [search_expr(x["name"]) for x in searchables] + [root_path(path) for path in related]
    if terms:
        lines += ["            if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {",
                  '                String needle = "%" + criteria.getSearch().trim().toLowerCase() + "%";',
                  "                predicates.add(cb.or("]
        for i, term in enumerate(terms):
            comma = "," if i < len(terms) - 1 else "));"
            lines.append(f"                        cb.like(cb.lower({term}), needle){comma}")
        lines.append("            }")
    # equality filters
    for rel in spec["relations"]:
        if rel["filter"]:
            lines += [f"            if (criteria.get{cap(rel['name'])}Id() != null) {{",
                      f"                predicates.add(cb.equal(root.get({jstr(rel['name'])}).get(\"id\"), criteria.get{cap(rel['name'])}Id()));",
                      "            }"]
    for x in spec["fields"]:
        if not x["filter"]:
            continue
        if x["type"] == "String":
            lines += [f"            if (criteria.get{cap(x['name'])}() != null && !criteria.get{cap(x['name'])}().isBlank()) {{",
                      f"                predicates.add(cb.equal(cb.lower(root.get({jstr(x['name'])})), criteria.get{cap(x['name'])}().trim().toLowerCase()));",
                      "            }"]
        else:
            lines += [f"            if (criteria.get{cap(x['name'])}() != null) {{",
                      f"                predicates.add(cb.equal(root.get({jstr(x['name'])}), criteria.get{cap(x['name'])}()));",
                      "            }"]
    for acc, t, path in spec.get("filters", []):
        lines += [f"            if (criteria.get{cap(acc)}() != null) {{",
                  f"                predicates.add(cb.equal({root_path(path)}, criteria.get{cap(acc)}()));",
                  "            }"]
    # ranges
    for x in spec["fields"]:
        if not x["range"]:
            continue
        n = x["name"]
        lines += [f"            if (criteria.getMin{cap(n)}() != null) {{",
                  f"                predicates.add(cb.greaterThanOrEqualTo(root.get({jstr(n)}), criteria.getMin{cap(n)}()));",
                  "            }"]
        lines += [f"            if (criteria.getMax{cap(n)}() != null) {{",
                  f"                predicates.add(cb.lessThanOrEqualTo(root.get({jstr(n)}), criteria.getMax{cap(n)}()));",
                  "            }"]
    if spec.get("related_search") or spec.get("related_sort"):
        lines += ["            if (RELATED_SORTABLE.contains(sortBy) && query.getResultType() != Long.class",
                  "                    && query.getResultType() != long.class) {",
                  '                String[] parts = sortBy.split("[.]");',
                  "                Path<Object> path = root.get(parts[0]);",
                  "                for (int i = 1; i < parts.length; i++) {",
                  "                    path = path.get(parts[i]);",
                  "                }",
                  "                query.orderBy(direction == Sort.Direction.ASC ? cb.asc(path) : cb.desc(path));",
                  "            }",
                  ""]
    lines += ["            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));",
              "        };",
              "    }", "}", ""]
    write(f"{domain}/services/Search{plural}.java", "\n".join(lines))


def search_expr(field_name):
    return f'root.get({jstr(field_name)})'


def root_path(path):
    parts = path.split(".")
    expr = "root"
    for p in parts:
        expr += f'.get({jstr(p)})'
    return expr


def gen_controller(spec):
    name, domain, plural = spec["name"], spec["pkg"], spec["plural"]
    v = var(name)
    imps = {"org.springframework.data.domain.Page", "org.springframework.http.HttpStatus",
            "org.springframework.security.access.prepost.PreAuthorize",
            "org.springframework.web.bind.annotation.*",
            f"{BASE}.common.PageResponse",
            f"{BASE}.{domain}.dto.{name}RequestDTO", f"{BASE}.{domain}.dto.{name}ResponseDTO",
            f"{BASE}.{domain}.dto.{name}SearchCriteria",
            f"{BASE}.{domain}.services.Create{name}", f"{BASE}.{domain}.services.Delete{name}",
            f"{BASE}.{domain}.services.Get{name}ById", f"{BASE}.{domain}.services.Search{plural}",
            f"{BASE}.{domain}.services.Update{name}", f"{BASE}.{domain}.utils.{name}Mapper",
            "lombok.RequiredArgsConstructor"}
    lines = [f"package {BASE}.{domain};", "", render_imports(imps),
             "@RestController", "@RequiredArgsConstructor",
             f"@RequestMapping(\"/api/meridian/{spec['path']}\")",
             f"public class {name}Controller {{", "",
             f"    private final Create{name} create{name};",
             f"    private final Update{name} update{name};",
             f"    private final Delete{name} delete{name};",
             f"    private final Get{name}ById get{name}ById;",
             f"    private final Search{plural} search{plural};",
             f"    private final {name}Mapper {v}Mapper;", "",
             "    @PostMapping",
             "    @ResponseStatus(HttpStatus.CREATED)",
             *([] if spec.get("open_create") else ["    @PreAuthorize(\"hasAnyRole('ADMIN','IT_SUPPORT')\")"]),
             f"    public {name}ResponseDTO create(@RequestBody {name}RequestDTO request) {{",
             f"        return {v}Mapper.toResponse(create{name}.create(request));",
             "    }", "",
             "    @GetMapping",
             f"    public PageResponse<{name}ResponseDTO> search({name}SearchCriteria criteria) {{",
             f"        Page<{name}> page = search{plural}.search(criteria);",
             f"        return PageResponse.of(page, {v}Mapper::toResponse);",
             "    }", "",
             "    @GetMapping(\"/{id}\")",
             f"    public {name}ResponseDTO get(@PathVariable Long id) {{",
             f"        return {v}Mapper.toResponse(get{name}ById.get(id));",
             "    }", "",
             "    @PutMapping(\"/{id}\")",
             "    @PreAuthorize(\"hasAnyRole('ADMIN','IT_SUPPORT')\")",
             f"    public {name}ResponseDTO update(@PathVariable Long id, @RequestBody {name}RequestDTO request) {{",
             f"        return {v}Mapper.toResponse(update{name}.update(id, request));",
             "    }", "",
             "    @DeleteMapping(\"/{id}\")",
             "    @ResponseStatus(HttpStatus.NO_CONTENT)",
             "    @PreAuthorize(\"hasRole('ADMIN')\")",
             "    public void delete(@PathVariable Long id) {",
             f"        delete{name}.delete(id);",
             "    }", "}", ""]
    write(f"{domain}/{name}Controller.java", "\n".join(lines))


# Entity name (lower-cased) -> package/directory name.
PKG_NAMES = {
    "academicsession": "academicSession",
    "auditlog": "auditLog",
    "feepayment": "feePayment",
    "gradescale": "gradeScale",
    "leaverequest": "leaveRequest",
    "ratelimit": "rateLimit",
    "resultsheet": "resultSheet",
    "schoolsettings": "schoolSettings",
    "supportticket": "supportTicket",
    "teachingassignment": "teachingAssignment",
    "termreport": "termReport",
    "timetableslot": "timetableSlot",
}


def target_pkg(entity_name):
    """Package of a related entity: camelCase where the folder was renamed."""
    return PKG_NAMES.get(entity_name.lower(), entity_name.lower())


def main():
    for spec in ENTITIES:
        spec = dict(spec)
        spec.setdefault("unique_together", [])
        spec.setdefault("extras", [])
        spec.setdefault("filters", [])
        spec.setdefault("module_gate", None)
        spec.setdefault("repo_methods", [])
        # The package is the entity name with a lower-case first letter, so the tree reads like the
        # domain (AcademicSession -> academicSession). "classroom" stays one word: it is a dictionary word.
        spec["pkg"] = PKG_NAMES.get(spec["name"].lower(), spec["name"].lower())
        spec.setdefault("open_create", False)
        spec.setdefault("cache_evict", [])
        if spec.get("autocode"):
            field(spec, spec["autocode"][0])["autocode_field"] = True
        gen_entity(spec)
        gen_repository(spec)
        gen_dtos(spec)
        gen_mapper(spec)
        gen_events(spec)
        gen_search(spec)
        gen_get(spec)
        if spec["crud"]:
            gen_create(spec)
            gen_update(spec)
            gen_delete(spec)
            gen_controller(spec)
        print("generated", spec["name"])


if __name__ == "__main__":
    main()
