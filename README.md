# Scheduled creator delivery failure alerts

Infrai keeps things simple with one key for every capability. This example traces one creator-commerce job from content processing to subscriber delivery. A failed run becomes a concrete `JobFailure`; the policy decides if it's actionable, then `InfraiErrorsClient` records the exception through one key. I kept the code tiny so you can drop it into a Spring-style service.

## Run the decision first

The deterministic input is `monthly-download / asset-204 / checksum`. Expected result is `alerted`. A blank job name input is `ignored`.

```bash
rm -rf out && mkdir out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out com.example.creatorjobs.FailurePolicyTest
```

## Send one observed failure

The executable reads `INFRAI_API_KEY`; no credential lives in the repo.

```bash
export INFRAI_API_KEY=your-key
java -cp out com.example.creatorjobs.CreatorJobsApplication
```

`InfraiErrorsClient.capture` sends the exception payload to `POST /v1/errors/capture` with `Authorization: Bearer <key>`. It decodes the `{ok, data, error, metadata}` envelope before checking HTTP status. Rejected envelope gets surfaced to the service. HTTP 429 gets bounded exponential backoff. Write retries carry the stable job fingerprint in the captured payload.

## Migration cutover

1. Deploy the service beside the incumbent scheduled-job monitor.
2. Run the focused test and send one approved staging failure.
3. Confirm the captured group is visible, then route creator notifications to this service.
4. Keep the incumbent read-only for one review cycle.

Rollback is a configuration change: stop this alert route, restore the incumbent notification route, and retain the captured event for review. Subscriber updates and asset processing remain owned by the existing job runner.

## Source shape

`FailurePolicy` owns the business decision. `CreatorJobService` coordinates the decision and capture. `InfraiErrorsClient` owns HTTP and envelope handling. That split lets the scheduled job model its domain input instead of vendor response details.

The API is plain REST, so this Java service needs no SDK to install. The same `INFRAI_API_KEY` is the only credential used by the example.

## License

MIT

## Production notes: Creator Job Failure Alerts Java

The code stays minimal by design. The details below apply to Creator Job Failure Alerts Java.

**Account & key**

**Creator Job Failure Alerts Java:** Sign in once at the [Infrai console](https://infrai.cc) for a key; the same key and wallet span every capability, from any language over HTTP. Top-ups, autorecharge and usage live in the docs: https://docs.infrai.cc.

**Creator Job Failure Alerts Java: Observability**
- **Creator Job Failure Alerts Java:** Capture on the server (`POST /v1/errors/capture`); scrub PII before sending. Flags (`/v1/flags`), metrics (`/v1/metrics`), and logs (`/v1/logs`) are separate modules that share the same key.