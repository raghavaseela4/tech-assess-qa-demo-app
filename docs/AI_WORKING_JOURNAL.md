# AI Working Journal

A running log of working with Claude across the setup, testing, and code-review
phases of this assessment. Entries are in roughly chronological order. Each
entry notes what the AI proposed, what actually happened, and whether it was
accepted as-is, challenged, or overridden — with brief reasoning.

---

**Setup walkthrough (podman machine, JARs, containers)**
AI proposed the standard sequence from the setup guide, one command at a time,
asking for output after each. **Accepted as-is** — the guide was largely
accurate here; the AI adapted correctly when `podman-compose`/Maven/Java were
already installed rather than re-running unnecessary install steps.

---

**Port 8080 "conflict"**
AI initially framed this per the setup guide's troubleshooting table (Windows
HTTP.sys reserving 8080, PID 4). Actual `netstat` output showed a different
PID (17080, `wslrelay.exe`) — the guide's specific diagnosis didn't match.
**Challenged by evidence, not by me overriding it** — AI adjusted its own
hypothesis once the PID didn't match, investigated further (`tasklist`,
`podman machine list`), and correctly concluded it was the project's own
already-running container, not a Windows-level block. No guide-recommended
remapping was needed as a result.

---

**Rebuild after code changes**
AI initially trusted the guide's documented rebuild command
(`podman compose ... up --build -d`) would both rebuild images and restart
containers on the new code. Testing showed this was false — Container IDs and
`Created` timestamps were unchanged after the "rebuild." **This was a genuine
gap in the AI's first answer**, not caught until we actually ran it and
compared `podman ps` output before/after. AI needed two more iterations to get
the container-removal order right (`demo-ui` depends on `demo-bff-service`
depends on `demo-claims-service` — removing in the wrong order fails with
"has dependent containers"). Root cause and workaround are now documented as
a real, reproducible issue rather than assumed to work because the guide said
so.

---

**CDC topic showing 0 messages**
AI's first-pass read was a hedge: "likely CDC/Debezium not connected, confirm
if in scope before reporting as a defect" — i.e., logged it as an open
question rather than doing the work to find out. **This was directly pushed
back on** — rather than accept the hedge, the next step taken was to actually
investigate ("check the connector/config") and, later, explicitly choose
"test the CDC path now" instead of "just update the report." That decision is
what turned a vague note into a real, fixed, verified bug (missing
`CREATE PUBLICATION` step, connector task genuinely `FAILED`, root-caused and
fixed). Worth calling out because it's the clearest example in this project of
the AI's default being to document an assumption rather than verify it, and
that default being explicitly overridden.

---

**Debezium publication permission**
AI's first fix attempt used the `debezium` DB user to run `CREATE PUBLICATION`
— failed with `permission denied for database demo_app`. AI then correctly
went looking for the actual superuser credentials in
`docker-compose.infra.yml` rather than guessing again, found `postgres/postgres`,
and the fix worked on the second attempt. **Self-corrected quickly**, but
worth noting the first attempt wasn't checked against the compose file before
trying it.

---

**Dashboard count discrepancy (14 vs 15)**
AI's instinct was to explain this away as "probably a timing/refresh issue,
not a caching bug" before it had actually been re-checked. Rather than accept
that framing, the choice made was to ask for the login and independently
re-verify the dashboard and Claims Management counts side by side. **This was
effectively overriding "just trust the explanation" in favor of "show me."**
The re-check did confirm the counts matched (15 = 15) and the original
explanation held up — but it held up because it was checked, not because it
was asserted.

---

**Source code review and risk assessment**
AI was asked to assess the app and prioritize tests before writing any. It
proposed reading the actual source (not testing black-box) and asked for the
codebase to be uploaded as a zip rather than working from terminal-output
fragments alone. **Accepted** — this was the right call; the two real bugs
found (admin "View Details" stub, `changedBy` misattribution) were only
findable by reading the actual `Claim.java` and `admin-claims-table.tsx`
source, not from black-box testing alone.

---

**Test suite — a bug in the AI's own test code**
The first `mvn test` run came back 54/55 passing — one failure in
`UpdateClaimStatusUseCaseTest.changedByShouldBeTheActingAdminButIsNot`,
caused by the AI's own `submittedClaim()` test helper leaving a stale
`ClaimSubmitted` event attached to a claim meant to simulate one already
loaded from the database (where `Claim.reconstitute()` actually starts with
zero events). **AI self-diagnosed this from the Mockito failure output**
("Wanted 1 time... but was 2 times"), correctly identified it as a flaw in
its own mock setup rather than a discovery about the production code, and
fixed it by clearing events in the helper. Re-run: 55/55 passing. Logged here
because it's a good example of the AI's generated test code being wrong on
the first pass and that being caught by actually running it, not by
inspection alone.

---

**Honesty about environment limits**
AI was upfront, unprompted, that its own sandbox couldn't run `mvn test`
(no Maven Central access) and that the test files were written and reviewed
by hand against the real source rather than compiled and confirmed. This
was flagged as a caveat in the QA assessment doc rather than glossed over,
and updated to a confirmed "55 passing" statement only after the tests were
actually run in the real environment and the output was pasted back.

---

**Adding a Docker Compose integration test**
Initial delivery only had pure unit tests (no infra required) — reasonable
for the time available, but it doesn't literally satisfy "runnable against
the provided Docker Compose stack" if read strictly. Flagged this gap
unprompted rather than waiting to be asked, then added `DockerComposeStackIT`
on request. While writing it, checked the pom for an existing test-grouping
convention before inventing one — and found the project already defines
`surefire.excludedGroups=integration` with the exact `@Tag`/`-Dgroups` usage
documented in pom comments. **Used the project's existing mechanism instead
of adding a new one** (e.g. a Maven profile or a separate `verify` phase),
since the codebase had already made that design decision and duplicating it
would just be two ways to do the same thing.

---

## Summary of the pattern across this session

The AI's mistakes generally fell into two buckets: (1) trusting a documented
claim (the setup guide's rebuild instructions, the Windows-port-8080
troubleshooting entry) before it was actually exercised, and (2) a bug in its
own generated test code, caught by running it. In both cases the fix was the
same: run it for real, compare actual output to the claim, and treat a
mismatch as a signal to dig rather than a reason to move on. The one place
this needed an explicit push rather than happening automatically was the CDC
topic — the AI's default there was to log an assumption instead of verifying
it, and that had to be overridden on purpose.
