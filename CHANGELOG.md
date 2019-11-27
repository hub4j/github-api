# Changelog

## [github-api-1.101](https://github.com/github-api/github-api/tree/github-api-1.101) (2019-11-27)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.100...github-api-1.101)

### Fixes

- Fixed `ClassNotFoundException` when creating `okhttp3.OkHttpConnector` with `Cache` @alecharp [\#627](https://github.com/github-api/github-api/issues/627)

## [github-api-1.100](https://github.com/github-api/github-api/tree/github-api-1.100) (2019-11-26)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.99...github-api-1.100)

### Features and Fixes

- Add method to set repository topics @martinvanzijl [\#594](https://github.com/github-api/github-api/issues/594)
- Adjust GHRateLimit to system time instead of depending on synchronization @bitwiseman [#595](https://github.com/github-api/github-api/issues/595)
- Add Functionality of OTP to support user 2fa @madhephaestus [\#603](https://github.com/github-api/github-api/issues/603)
- Implement Meta endpoint @PauloMigAlmeida [\#611](https://github.com/github-api/github-api/issues/611)
- fix and unit tests for issue #504 @siordache [\#620](https://github.com/github-api/github-api/issues/620)
- Fixed GHContent to allow spaces in path @bitwiseman [\#625](https://github.com/github-api/github-api/issues/625)

### Internals

- Bump okhttp3 from 3.14.2 to 4.2.2 @dependabot-preview [\#593](https://github.com/github-api/github-api/issues/593)
- jackson 2.10.1 @sullis [\#604](https://github.com/github-api/github-api/issues/604)
- Code style fixes @bitwiseman [\#609](https://github.com/github-api/github-api/issues/609)
- Javadoc fail on warning during CI build @bitwiseman [\#613](https://github.com/github-api/github-api/issues/613)
- Clean up Requester interface a bit @bitwiseman [\#614](https://github.com/github-api/github-api/issues/614)
- Branch missing @alexanderrtaylor [\#615](https://github.com/github-api/github-api/issues/615)
- Cleanup imports @bitwiseman [\#616](https://github.com/github-api/github-api/issues/616)
- Removed permission field in createTeam. It is deprecated in the API @asthinasthi [\#619](https://github.com/github-api/github-api/issues/619)
- Re-enable Lifecycle test @bitwiseman [\#621](https://github.com/github-api/github-api/issues/621)


## [github-api-1.99](https://github.com/github-api/github-api/tree/github-api-1.99) (2019-11-04)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.95...github-api-1.99)

**Closed issues:**

- Support all available endpoints for Github App with preview request [\#570](https://github.com/github-api/github-api/issues/570)
- Login details [\#560](https://github.com/github-api/github-api/issues/560)
- GHRepository.listReleases\(\) return empty always [\#535](https://github.com/github-api/github-api/issues/535)
- Unable to get deployment by id [\#529](https://github.com/github-api/github-api/issues/529)
- Malformed URL exception while accessing Enterprise Repository and fetching data [\#526](https://github.com/github-api/github-api/issues/526)
- Allow getting a repository by ID [\#515](https://github.com/github-api/github-api/issues/515)
- Methods to update milestones [\#512](https://github.com/github-api/github-api/issues/512)
- Add ETAG support to minimize API requests [\#505](https://github.com/github-api/github-api/issues/505)
- GitHub.connectUsingOAuth\(\) suddenly taking a really long time to connect [\#493](https://github.com/github-api/github-api/issues/493)
- GHTeam.add does not due to GHTeam.Role\(s\) been capitalized [\#489](https://github.com/github-api/github-api/issues/489)
- Reading file's content through GHContent.read\(\) returns previous version of file. [\#487](https://github.com/github-api/github-api/issues/487)
- Implement archive/unarchive functionality [\#472](https://github.com/github-api/github-api/issues/472)
- \[Gists\] Edit Gists Support [\#466](https://github.com/github-api/github-api/issues/466)
- Missing description field in GHTeam [\#460](https://github.com/github-api/github-api/issues/460)
- Bug: GHOrganization::createTeam does not regard argument repositories [\#457](https://github.com/github-api/github-api/issues/457)
- Null value for GHPullRequestReview created date and updated date [\#450](https://github.com/github-api/github-api/issues/450)
- Support for repository Projects [\#425](https://github.com/github-api/github-api/issues/425)
- create a little MockGitHub class for tests mocking out the github REST API [\#382](https://github.com/github-api/github-api/issues/382)
- Branch name is not being correctly URL encoded [\#381](https://github.com/github-api/github-api/issues/381)
- Issue events [\#376](https://github.com/github-api/github-api/issues/376)
- Not able to get the right file content [\#371](https://github.com/github-api/github-api/issues/371)
- Updating file is not possible [\#354](https://github.com/github-api/github-api/issues/354)
- Missing repository statistics [\#330](https://github.com/github-api/github-api/issues/330)
- Is there a way to make this library more test friendly? [\#316](https://github.com/github-api/github-api/issues/316)
- GitHub 2 factor login [\#292](https://github.com/github-api/github-api/issues/292)
- Unable to resolve github-api artifacts from Maven Central [\#195](https://github.com/github-api/github-api/issues/195)

**Merged pull requests:**

- Bump maven-source-plugin from 3.1.0 to 3.2.0 [\#590](https://github.com/github-api/github-api/pull/590) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Fix site errors [\#587](https://github.com/github-api/github-api/pull/587) ([bitwiseman](https://github.com/bitwiseman))
- \[Documentation\] :: Add GitHub App Developer Guide [\#586](https://github.com/github-api/github-api/pull/586) ([PauloMigAlmeida](https://github.com/PauloMigAlmeida))
- Create CODE\_OF\_CONDUCT.md [\#585](https://github.com/github-api/github-api/pull/585) ([bitwiseman](https://github.com/bitwiseman))
- Convenience method to auth with app installation token && documentation examples [\#583](https://github.com/github-api/github-api/pull/583) ([PauloMigAlmeida](https://github.com/PauloMigAlmeida))
- Add method to list repository topics [\#581](https://github.com/github-api/github-api/pull/581) ([martinvanzijl](https://github.com/martinvanzijl))
- Fix for getting deployment by id [\#580](https://github.com/github-api/github-api/pull/580) ([martinvanzijl](https://github.com/martinvanzijl))
- Add methods to update and delete milestones. [\#579](https://github.com/github-api/github-api/pull/579) ([martinvanzijl](https://github.com/martinvanzijl))
- GHOrganization.createTeam now adds team to specified repositories [\#578](https://github.com/github-api/github-api/pull/578) ([martinvanzijl](https://github.com/martinvanzijl))
- bump jackson-databind to 2.10.0 to avoid security alert [\#575](https://github.com/github-api/github-api/pull/575) ([romani](https://github.com/romani))
- Bump wiremock-jre8-standalone from 2.25.0 to 2.25.1 [\#574](https://github.com/github-api/github-api/pull/574) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump hamcrest.version from 2.1 to 2.2 [\#573](https://github.com/github-api/github-api/pull/573) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- GitHub workflow: add JDK 13 to build matrix [\#572](https://github.com/github-api/github-api/pull/572) ([sullis](https://github.com/sullis))
- More tests [\#568](https://github.com/github-api/github-api/pull/568) ([bitwiseman](https://github.com/bitwiseman))
- Add merge options to GHRepository [\#567](https://github.com/github-api/github-api/pull/567) ([jberglund-BSFT](https://github.com/jberglund-BSFT))
- Bump gson from 2.8.5 to 2.8.6 [\#565](https://github.com/github-api/github-api/pull/565) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump okio from 2.4.0 to 2.4.1 [\#564](https://github.com/github-api/github-api/pull/564) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Simplify creation of PagedIterables from requests [\#563](https://github.com/github-api/github-api/pull/563) ([bitwiseman](https://github.com/bitwiseman))
- GitHub workflow: enable Java matrix \[ '1.8.0', '11.0.x' \] [\#562](https://github.com/github-api/github-api/pull/562) ([sullis](https://github.com/sullis))
- Bump org.eclipse.jgit from 5.5.0.201909110433-r to 5.5.1.201910021850-r [\#561](https://github.com/github-api/github-api/pull/561) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump okio from 2.2.2 to 2.4.0 [\#558](https://github.com/github-api/github-api/pull/558) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 3.0.0 to 3.1.0 [\#557](https://github.com/github-api/github-api/pull/557) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump wiremock-jre8-standalone from 2.24.1 to 2.25.0 [\#556](https://github.com/github-api/github-api/pull/556) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump commons-io from 1.4 to 2.6 [\#555](https://github.com/github-api/github-api/pull/555) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump maven-surefire-plugin from 2.22.1 to 2.22.2 [\#554](https://github.com/github-api/github-api/pull/554) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump commons-lang3 from 3.7 to 3.9 [\#552](https://github.com/github-api/github-api/pull/552) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump commons-codec from 1.7 to 1.13 [\#551](https://github.com/github-api/github-api/pull/551) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spotbugs-maven-plugin from 3.1.11 to 3.1.12.2 [\#550](https://github.com/github-api/github-api/pull/550) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump bridge-method-annotation from 1.17 to 1.18 [\#549](https://github.com/github-api/github-api/pull/549) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump org.eclipse.jgit from 4.9.0.201710071750-r to 5.5.0.201909110433-r [\#547](https://github.com/github-api/github-api/pull/547) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Support for projects [\#545](https://github.com/github-api/github-api/pull/545) ([gskjold](https://github.com/gskjold))
- Adding possiblity to get ssh keys [\#544](https://github.com/github-api/github-api/pull/544) ([arngrimur-seal](https://github.com/arngrimur-seal))
- Grammar [\#543](https://github.com/github-api/github-api/pull/543) ([jsoref](https://github.com/jsoref))
- Improved OkHttpConnector caching behavior [\#542](https://github.com/github-api/github-api/pull/542) ([bitwiseman](https://github.com/bitwiseman))
- Add GitHubApiWireMockRule [\#541](https://github.com/github-api/github-api/pull/541) ([bitwiseman](https://github.com/bitwiseman))
- Add support for team pr review requests [\#532](https://github.com/github-api/github-api/pull/532) ([farmdawgnation](https://github.com/farmdawgnation))
- Add GitHub API requests logging [\#530](https://github.com/github-api/github-api/pull/530) ([bozaro](https://github.com/bozaro))
- Add support for draft pull requests [\#525](https://github.com/github-api/github-api/pull/525) ([vbehar](https://github.com/vbehar))
- Implement GitHub App API methods [\#522](https://github.com/github-api/github-api/pull/522) ([PauloMigAlmeida](https://github.com/PauloMigAlmeida))
- Added getUserPublicOrganizations method [\#510](https://github.com/github-api/github-api/pull/510) ([awittha](https://github.com/awittha))
- Add support for editing Gists [\#484](https://github.com/github-api/github-api/pull/484) ([martinvanzijl](https://github.com/martinvanzijl))
- Add method to invite user to organization [\#482](https://github.com/github-api/github-api/pull/482) ([martinvanzijl](https://github.com/martinvanzijl))
- Added method to list authorizations [\#481](https://github.com/github-api/github-api/pull/481) ([martinvanzijl](https://github.com/martinvanzijl))
- Escape special characters in branch URLs [\#480](https://github.com/github-api/github-api/pull/480) ([martinvanzijl](https://github.com/martinvanzijl))
- Add issue events API [\#479](https://github.com/github-api/github-api/pull/479) ([martinvanzijl](https://github.com/martinvanzijl))
- Added description field to GHTeam class. [\#478](https://github.com/github-api/github-api/pull/478) ([martinvanzijl](https://github.com/martinvanzijl))
- Add statistics API. [\#477](https://github.com/github-api/github-api/pull/477) ([martinvanzijl](https://github.com/martinvanzijl))
- Adding Label description property [\#475](https://github.com/github-api/github-api/pull/475) ([immanuelqrw](https://github.com/immanuelqrw))
- Implemented GitHub.doArchive [\#473](https://github.com/github-api/github-api/pull/473) ([joaoe](https://github.com/joaoe))

## [github-api-1.95](https://github.com/github-api/github-api/tree/github-api-1.95) (2018-11-06)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.94...github-api-1.95)

**Closed issues:**

- Add ability to attach/detach issue label w/o side effects to other labels [\#456](https://github.com/github-api/github-api/issues/456)
- \[feature request\] Add support to list commits that only affect a file path [\#454](https://github.com/github-api/github-api/issues/454)
- Rate limit - catch the exception [\#447](https://github.com/github-api/github-api/issues/447)
- GHRepository.listCommits\(\) returns java.net.SocketTimeoutException: Read timed out [\#433](https://github.com/github-api/github-api/issues/433)
- java.lang.NoSuchMethodError: com.fasterxml.jackson.databind.deser.SettableBeanProperty.\<init\> [\#419](https://github.com/github-api/github-api/issues/419)
- mvn test fails to start on HEAD [\#415](https://github.com/github-api/github-api/issues/415)
- NullPointerException in org.kohsuke.github.GHContent.read [\#414](https://github.com/github-api/github-api/issues/414)

**Merged pull requests:**

- Added archived attribute in GHRepository [\#470](https://github.com/github-api/github-api/pull/470) ([recena](https://github.com/recena))
- Fix memory leak. [\#468](https://github.com/github-api/github-api/pull/468) ([KostyaSha](https://github.com/KostyaSha))
- add request reviewers as attribute of GHPullRequest [\#464](https://github.com/github-api/github-api/pull/464) ([xeric](https://github.com/xeric))
- Add methods for adding/removing labels to GHIssue [\#461](https://github.com/github-api/github-api/pull/461) ([evenh](https://github.com/evenh))

## [github-api-1.94](https://github.com/github-api/github-api/tree/github-api-1.94) (2018-08-30)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.93...github-api-1.94)

**Closed issues:**

- Attachment download from issues [\#451](https://github.com/github-api/github-api/issues/451)
- GHEventPayload.Issue [\#442](https://github.com/github-api/github-api/issues/442)
- GHRelease.uploadAsset\(\) that takes InputStream instead of File [\#434](https://github.com/github-api/github-api/issues/434)
- Implement the new invitations API [\#374](https://github.com/github-api/github-api/issues/374)

**Merged pull requests:**

- Fix for issue \#426. Fix null pointer when deleting refs. [\#449](https://github.com/github-api/github-api/pull/449) ([martinvanzijl](https://github.com/martinvanzijl))
- Fix pagination for APIs that supported it ad hoc [\#446](https://github.com/github-api/github-api/pull/446) ([daniel-beck](https://github.com/daniel-beck))
- Adds the GHEventPayload.Issue class [\#443](https://github.com/github-api/github-api/pull/443) ([Arrow768](https://github.com/Arrow768))
- support update content through createContent, passing sha of existing file [\#441](https://github.com/github-api/github-api/pull/441) ([shirdoo](https://github.com/shirdoo))
- Add support for repository searching by "topic" [\#439](https://github.com/github-api/github-api/pull/439) ([l3ender](https://github.com/l3ender))
- - added overloaded 'uploadAsset' method [\#438](https://github.com/github-api/github-api/pull/438) ([jgangemi](https://github.com/jgangemi))
- \[feature\] implement Repository Invitations API [\#437](https://github.com/github-api/github-api/pull/437) ([Rechi](https://github.com/Rechi))
- - remove unthrown IOException [\#436](https://github.com/github-api/github-api/pull/436) ([jgangemi](https://github.com/jgangemi))
- - branch protection enhancements [\#435](https://github.com/github-api/github-api/pull/435) ([jgangemi](https://github.com/jgangemi))
- Added release payload. [\#417](https://github.com/github-api/github-api/pull/417) ([twcurrie](https://github.com/twcurrie))
- Add GHRepository.getRelease and GHRepository.getReleaseByTagName [\#411](https://github.com/github-api/github-api/pull/411) ([tadfisher](https://github.com/tadfisher))

## [github-api-1.93](https://github.com/github-api/github-api/tree/github-api-1.93) (2018-05-01)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.92...github-api-1.93)

**Closed issues:**

- InvalidFormatException parsing GHEventPayload.PullRequestReview [\#421](https://github.com/github-api/github-api/issues/421)
- https://api.github.com/user  response code -1 [\#418](https://github.com/github-api/github-api/issues/418)
- Update commons-lang version [\#409](https://github.com/github-api/github-api/issues/409)
- add a comment to a pull Request [\#380](https://github.com/github-api/github-api/issues/380)

**Merged pull requests:**

- \[fix\] fetch labels with HTTP GET method [\#431](https://github.com/github-api/github-api/pull/431) ([Rechi](https://github.com/Rechi))
- Added request reviewers function within GHPullRequest. [\#430](https://github.com/github-api/github-api/pull/430) ([twcurrie](https://github.com/twcurrie))
- Add support for previous\_filename for file details in PR. [\#427](https://github.com/github-api/github-api/pull/427) ([itepikin-smartling](https://github.com/itepikin-smartling))
- Fixes \#421 - Enum case doesn't match for Pull Request Reviews [\#422](https://github.com/github-api/github-api/pull/422) ([ggrell](https://github.com/ggrell))
- OkHttpConnector: Enforce use of TLSv1.2 to match current Github and Github Enterprise TLS support. [\#420](https://github.com/github-api/github-api/pull/420) ([randomvariable](https://github.com/randomvariable))
- Update commons-lang to 3.7 [\#410](https://github.com/github-api/github-api/pull/410) ([Limess](https://github.com/Limess))

## [github-api-1.92](https://github.com/github-api/github-api/tree/github-api-1.92) (2018-01-13)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.91...github-api-1.92)

## [github-api-1.91](https://github.com/github-api/github-api/tree/github-api-1.91) (2018-01-13)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.90...github-api-1.91)

**Closed issues:**

- How to authenticate using oauth in code? [\#405](https://github.com/github-api/github-api/issues/405)
- It is possible to read a github project wiki ? [\#404](https://github.com/github-api/github-api/issues/404)
- gitHttpTransportUrl Rename [\#403](https://github.com/github-api/github-api/issues/403)
- Do not throw new Error\(\) [\#400](https://github.com/github-api/github-api/issues/400)
- GHPullRequest.getMergeable\(\) never returns True [\#399](https://github.com/github-api/github-api/issues/399)
- NPE in GHPerson.populate [\#395](https://github.com/github-api/github-api/issues/395)
- 64-bit id support [\#393](https://github.com/github-api/github-api/issues/393)
- Numeric value out of range of int [\#387](https://github.com/github-api/github-api/issues/387)
- Diff URL with auth [\#386](https://github.com/github-api/github-api/issues/386)

**Merged pull requests:**

- Adding merge settings to GHCreateRepositoryBuilder [\#407](https://github.com/github-api/github-api/pull/407) ([notsudo](https://github.com/notsudo))
- Improved Pull Request review and comments support [\#406](https://github.com/github-api/github-api/pull/406) ([sns-seb](https://github.com/sns-seb))
- Add GHIssue\#setMilestone [\#397](https://github.com/github-api/github-api/pull/397) ([mizoguche](https://github.com/mizoguche))
- \[fix\] GHPerson: check if root is null [\#396](https://github.com/github-api/github-api/pull/396) ([Rechi](https://github.com/Rechi))
- Add get for all organizations [\#391](https://github.com/github-api/github-api/pull/391) ([scotty-g](https://github.com/scotty-g))
- Add support for pr review/review comment events [\#384](https://github.com/github-api/github-api/pull/384) ([mattnelson](https://github.com/mattnelson))
- Roles for team members [\#379](https://github.com/github-api/github-api/pull/379) ([amberovsky](https://github.com/amberovsky))

## [github-api-1.90](https://github.com/github-api/github-api/tree/github-api-1.90) (2017-10-28)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.89...github-api-1.90)

**Closed issues:**

- \<null\> fields yeld NPE on getX operations. [\#372](https://github.com/github-api/github-api/issues/372)
- Add support for committing multiple files. [\#360](https://github.com/github-api/github-api/issues/360)
- Update compiler [\#357](https://github.com/github-api/github-api/issues/357)
- Extension mechanism? [\#356](https://github.com/github-api/github-api/issues/356)
- Update GHPullRequest with missing properties [\#355](https://github.com/github-api/github-api/issues/355)
- Refactor to allow for additional HTTP headers [\#353](https://github.com/github-api/github-api/issues/353)
- Building failing due problematic repository server [\#344](https://github.com/github-api/github-api/issues/344)
- Pull Request Reviews API [\#305](https://github.com/github-api/github-api/issues/305)

**Merged pull requests:**

- Labels: add method to update color [\#390](https://github.com/github-api/github-api/pull/390) ([batmat](https://github.com/batmat))
- Fixed OAuth connection to enterprise API [\#389](https://github.com/github-api/github-api/pull/389) ([dorian808080](https://github.com/dorian808080))
- Fix for \#387: numeric value out of range of int [\#388](https://github.com/github-api/github-api/pull/388) ([aburmeis](https://github.com/aburmeis))

## [github-api-1.89](https://github.com/github-api/github-api/tree/github-api-1.89) (2017-09-09)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.88...github-api-1.89)

**Closed issues:**

- OkHttpConnector is broken [\#335](https://github.com/github-api/github-api/issues/335)
- Support new merge methods \(squash/rebase\) for Github Pull Requests [\#326](https://github.com/github-api/github-api/issues/326)
- Expose OAuth headers [\#303](https://github.com/github-api/github-api/issues/303)

**Merged pull requests:**

- Added support for traffic statistics \(number of views and clones\) [\#368](https://github.com/github-api/github-api/pull/368) ([adw1n](https://github.com/adw1n))
- issue \#360: Add support for committing multiple files [\#361](https://github.com/github-api/github-api/pull/361) ([siordache](https://github.com/siordache))
- Expose Headers [\#339](https://github.com/github-api/github-api/pull/339) ([KostyaSha](https://github.com/KostyaSha))
- Remove unused imports [\#337](https://github.com/github-api/github-api/pull/337) ([sebkur](https://github.com/sebkur))
- Add support for MergeMethod on GHPullRequest [\#333](https://github.com/github-api/github-api/pull/333) ([greggian](https://github.com/greggian))
- Add some level of synchronization to the root of the API [\#283](https://github.com/github-api/github-api/pull/283) ([mmitche](https://github.com/mmitche))

## [github-api-1.88](https://github.com/github-api/github-api/tree/github-api-1.88) (2017-09-09)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.87...github-api-1.88)

## [github-api-1.87](https://github.com/github-api/github-api/tree/github-api-1.87) (2017-09-09)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.86...github-api-1.87)

**Closed issues:**

- Cannot merge newly created GHPullRequest [\#367](https://github.com/github-api/github-api/issues/367)
- Unable to search thru search API [\#365](https://github.com/github-api/github-api/issues/365)
- Unable to find documentation for issue search [\#364](https://github.com/github-api/github-api/issues/364)
- Commit Search API implemented or not? [\#345](https://github.com/github-api/github-api/issues/345)
- How can I get Latest Release of Repository? [\#341](https://github.com/github-api/github-api/issues/341)

**Merged pull requests:**

- bridge-method-annotation should be an optional dep [\#378](https://github.com/github-api/github-api/pull/378) ([jglick](https://github.com/jglick))
- Add basic support for tag objects [\#375](https://github.com/github-api/github-api/pull/375) ([stephenc](https://github.com/stephenc))
- - improve branch protection support [\#369](https://github.com/github-api/github-api/pull/369) ([jgangemi](https://github.com/jgangemi))
- Add missing event types used by repository webhooks [\#363](https://github.com/github-api/github-api/pull/363) ([PauloMigAlmeida](https://github.com/PauloMigAlmeida))
- Add ping hook method [\#362](https://github.com/github-api/github-api/pull/362) ([KostyaSha](https://github.com/KostyaSha))
- \[JENKINS-36240\] /repos/:owner/:repo/collaborators/:username/permission no longer requires korra preview [\#358](https://github.com/github-api/github-api/pull/358) ([jglick](https://github.com/jglick))
- Add support for PR reviews preview [\#352](https://github.com/github-api/github-api/pull/352) ([stephenc](https://github.com/stephenc))
- Add the Commit search API [\#351](https://github.com/github-api/github-api/pull/351) ([mdeverdelhan](https://github.com/mdeverdelhan))
- add latest release [\#343](https://github.com/github-api/github-api/pull/343) ([kamontat](https://github.com/kamontat))
- Ignore eclipse files [\#338](https://github.com/github-api/github-api/pull/338) ([sebkur](https://github.com/sebkur))
- Fix a bug in the Javadocs \(due to copy and paste\) [\#332](https://github.com/github-api/github-api/pull/332) ([sebkur](https://github.com/sebkur))

## [github-api-1.86](https://github.com/github-api/github-api/tree/github-api-1.86) (2017-07-03)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.85...github-api-1.86)

**Merged pull requests:**

- \[JENKINS-45142\] Retry connections after getting SocketTimeoutException [\#359](https://github.com/github-api/github-api/pull/359) ([jglick](https://github.com/jglick))

## [github-api-1.85](https://github.com/github-api/github-api/tree/github-api-1.85) (2017-03-01)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.84...github-api-1.85)

**Closed issues:**

- getPusher\(\) returns null in call to getPusher\(\) [\#342](https://github.com/github-api/github-api/issues/342)

**Merged pull requests:**

- Ensure that connections are closed for error responses [\#346](https://github.com/github-api/github-api/pull/346) ([stephenc](https://github.com/stephenc))
- Correct algebra in \#327 [\#329](https://github.com/github-api/github-api/pull/329) ([stephenc](https://github.com/stephenc))

## [github-api-1.84](https://github.com/github-api/github-api/tree/github-api-1.84) (2017-01-10)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.83...github-api-1.84)

## [github-api-1.83](https://github.com/github-api/github-api/tree/github-api-1.83) (2017-01-10)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.82...github-api-1.83)

**Closed issues:**

- Webhook creation response error [\#328](https://github.com/github-api/github-api/issues/328)

**Merged pull requests:**

- Expose Rate Limit Headers [\#327](https://github.com/github-api/github-api/pull/327) ([stephenc](https://github.com/stephenc))
- Branch protection attrs [\#325](https://github.com/github-api/github-api/pull/325) ([jeffnelson](https://github.com/jeffnelson))
- \[JENKINS-36240\] Added GHRepository.getPermission\(String\) [\#324](https://github.com/github-api/github-api/pull/324) ([jglick](https://github.com/jglick))

## [github-api-1.82](https://github.com/github-api/github-api/tree/github-api-1.82) (2016-12-17)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.81...github-api-1.82)

**Closed issues:**

- API response time [\#322](https://github.com/github-api/github-api/issues/322)
- getLabels\(\) call for a pull request results in downstream 404s [\#319](https://github.com/github-api/github-api/issues/319)
- Gist Searching [\#318](https://github.com/github-api/github-api/issues/318)
- Adding users to organization-owned repos not possible [\#317](https://github.com/github-api/github-api/issues/317)
- GHSearchBuilder terms are cumulative when I expected them to overwrite previous one [\#314](https://github.com/github-api/github-api/issues/314)
- Support ordering in searches [\#313](https://github.com/github-api/github-api/issues/313)
- Update OkHttp usage [\#312](https://github.com/github-api/github-api/issues/312)
- github.getRepository does not work for Enterprise github instance [\#263](https://github.com/github-api/github-api/issues/263)

**Merged pull requests:**

- Fix syntactically malformed test JSON [\#323](https://github.com/github-api/github-api/pull/323) ([jglick](https://github.com/jglick))
- Added ghRepo.getBlob\(String\) method [\#320](https://github.com/github-api/github-api/pull/320) ([KostyaSha](https://github.com/KostyaSha))
- Fix typos in javadocs [\#315](https://github.com/github-api/github-api/pull/315) ([davidxia](https://github.com/davidxia))

## [github-api-1.81](https://github.com/github-api/github-api/tree/github-api-1.81) (2016-11-21)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.80...github-api-1.81)

**Closed issues:**

- Multiple assignee support [\#294](https://github.com/github-api/github-api/issues/294)
- Missing support for determining if authenticated user is organization owner [\#293](https://github.com/github-api/github-api/issues/293)

## [github-api-1.80](https://github.com/github-api/github-api/tree/github-api-1.80) (2016-11-17)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.79...github-api-1.80)

**Closed issues:**

- Testing reaction [\#311](https://github.com/github-api/github-api/issues/311)
- Is there a way to update contents in bulk? [\#310](https://github.com/github-api/github-api/issues/310)
- GitHub\#listAllUsers\(\) demanded \(enterprise use-case\) [\#309](https://github.com/github-api/github-api/issues/309)
- Delete OAuth Token [\#308](https://github.com/github-api/github-api/issues/308)
- How to find a pull request using the Search API and get its details? [\#298](https://github.com/github-api/github-api/issues/298)

**Merged pull requests:**

- Add portion of auth/application API. [\#307](https://github.com/github-api/github-api/pull/307) ([KostyaSha](https://github.com/KostyaSha))
- Add offline support to the API to make parsing events easier [\#306](https://github.com/github-api/github-api/pull/306) ([stephenc](https://github.com/stephenc))
- Fix fields of GHRepository [\#304](https://github.com/github-api/github-api/pull/304) ([wolfogre](https://github.com/wolfogre))

## [github-api-1.79](https://github.com/github-api/github-api/tree/github-api-1.79) (2016-10-25)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.78...github-api-1.79)

**Merged pull requests:**

- url encode hashes in ref names [\#299](https://github.com/github-api/github-api/pull/299) ([bsheats](https://github.com/bsheats))

## [github-api-1.78](https://github.com/github-api/github-api/tree/github-api-1.78) (2016-10-24)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.77...github-api-1.78)

**Closed issues:**

- Allow edit for maintainer support [\#297](https://github.com/github-api/github-api/issues/297)
- run mvn install fail! Failure to find org.jenkins-ci:jenkins:pom:1.26 ?? [\#296](https://github.com/github-api/github-api/issues/296)

**Merged pull requests:**

- Expose the commit dates [\#300](https://github.com/github-api/github-api/pull/300) ([stephenc](https://github.com/stephenc))
- Use maximum permitted page size [\#295](https://github.com/github-api/github-api/pull/295) ([jglick](https://github.com/jglick))

## [github-api-1.77](https://github.com/github-api/github-api/tree/github-api-1.77) (2016-08-06)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.76...github-api-1.77)

**Closed issues:**

- weird format for get list of organizations [\#291](https://github.com/github-api/github-api/issues/291)
- OkHttp is out of date [\#290](https://github.com/github-api/github-api/issues/290)
- 400 from OKHttp getInputStream [\#288](https://github.com/github-api/github-api/issues/288)
- pagination support search APIs [\#287](https://github.com/github-api/github-api/issues/287)
- significant more API calls for same code [\#286](https://github.com/github-api/github-api/issues/286)
- Excessive concurrent request rate limit not handled [\#285](https://github.com/github-api/github-api/issues/285)
- team.add\(repo\) should accept permission flag [\#279](https://github.com/github-api/github-api/issues/279)
- Pull request mergeability is boolean but should be trinary [\#275](https://github.com/github-api/github-api/issues/275)
- Webhook with content type "application/json" [\#274](https://github.com/github-api/github-api/issues/274)
- Disable rate\_limit check on GitHub Enterprise completely [\#273](https://github.com/github-api/github-api/issues/273)
- java.lang.IllegalArgumentException: byteString == null [\#265](https://github.com/github-api/github-api/issues/265)
- Failed to deserialize list of contributors when repo is empty [\#261](https://github.com/github-api/github-api/issues/261)
- github-api does not distinguish between user and organisation [\#260](https://github.com/github-api/github-api/issues/260)
- API Rate Limit Exceeding [\#258](https://github.com/github-api/github-api/issues/258)

**Merged pull requests:**

- Implement an abuse handler [\#289](https://github.com/github-api/github-api/pull/289) ([mmitche](https://github.com/mmitche))

## [github-api-1.76](https://github.com/github-api/github-api/tree/github-api-1.76) (2016-06-03)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.75...github-api-1.76)

**Closed issues:**

- GitHub.getRateLimit hangs inside SocketInputStream.socketRead0 [\#271](https://github.com/github-api/github-api/issues/271)
- \(re\)open method on GHMilestone [\#269](https://github.com/github-api/github-api/issues/269)
- More meaning toString implementations [\#268](https://github.com/github-api/github-api/issues/268)
- GHRepository.fork\(\) does not block on the async operation until it's complete [\#264](https://github.com/github-api/github-api/issues/264)
- Add Support for the Protected Branches API [\#262](https://github.com/github-api/github-api/issues/262)

**Merged pull requests:**

- related to JENKINS-34834. updating test for similar condition [\#282](https://github.com/github-api/github-api/pull/282) ([apemberton](https://github.com/apemberton))
- Add Slug to GHTeam per v3 API: https://developer.github.com/v3/orgs/t… [\#281](https://github.com/github-api/github-api/pull/281) ([apemberton](https://github.com/apemberton))
- Fixed broken link [\#278](https://github.com/github-api/github-api/pull/278) ([jglick](https://github.com/jglick))
- Updated Date was wrong [\#277](https://github.com/github-api/github-api/pull/277) ([KondaReddyR](https://github.com/KondaReddyR))
- Add support to delete a team [\#276](https://github.com/github-api/github-api/pull/276) ([thug-gamer](https://github.com/thug-gamer))
- Added support for the extended stargazers API in Github V3 API [\#272](https://github.com/github-api/github-api/pull/272) ([noctarius](https://github.com/noctarius))
- \[\#269\] Add reopen method on GHMilestone [\#270](https://github.com/github-api/github-api/pull/270) ([szpak](https://github.com/szpak))

## [github-api-1.75](https://github.com/github-api/github-api/tree/github-api-1.75) (2016-04-13)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.74...github-api-1.75)

## [github-api-1.74](https://github.com/github-api/github-api/tree/github-api-1.74) (2016-03-19)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.73...github-api-1.74)

**Closed issues:**

- missing maven central dependencies in 1.72 [\#257](https://github.com/github-api/github-api/issues/257)
- Not able to specify client Id and client secret to connect using OAuth [\#256](https://github.com/github-api/github-api/issues/256)
- Stuck in  Github connect [\#255](https://github.com/github-api/github-api/issues/255)
- Infinite loop in `GHNotificationStream$1.fetch\(\)` [\#252](https://github.com/github-api/github-api/issues/252)
- How to get statistics using this library [\#241](https://github.com/github-api/github-api/issues/241)

**Merged pull requests:**

- Animal sniffer [\#259](https://github.com/github-api/github-api/pull/259) ([Shredder121](https://github.com/Shredder121))
- Better error messages [\#254](https://github.com/github-api/github-api/pull/254) ([cyrille-leclerc](https://github.com/cyrille-leclerc))
- Fix \#252: infinite loop because the "hypertext engine" generates invalid URLs [\#253](https://github.com/github-api/github-api/pull/253) ([cyrille-leclerc](https://github.com/cyrille-leclerc))
- Improve checkApiUrlValidity\(\) method  [\#251](https://github.com/github-api/github-api/pull/251) ([recena](https://github.com/recena))

## [github-api-1.73](https://github.com/github-api/github-api/tree/github-api-1.73) (2016-03-01)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.72...github-api-1.73)

**Closed issues:**

- traceback if webhook set to "send me everything". [\#250](https://github.com/github-api/github-api/issues/250)
- Error on github pull request populate [\#246](https://github.com/github-api/github-api/issues/246)
- How to avoid connection timeout while using github.listallpublicrepositories  [\#243](https://github.com/github-api/github-api/issues/243)
- myissues [\#242](https://github.com/github-api/github-api/issues/242)
- Question - Stargazers and stars release. [\#239](https://github.com/github-api/github-api/issues/239)
- GHEventInfo getPayload [\#238](https://github.com/github-api/github-api/issues/238)

**Merged pull requests:**

- Added getHtmlUrl\(\) to GHCommit [\#249](https://github.com/github-api/github-api/pull/249) ([zapelin](https://github.com/zapelin))
- Populate commit with data for getCommitShortInfo [\#248](https://github.com/github-api/github-api/pull/248) ([daniel-beck](https://github.com/daniel-beck))
- Fix error when creating email service hook [\#245](https://github.com/github-api/github-api/pull/245) ([daniel-beck](https://github.com/daniel-beck))
- Minor amendment to the documentation [\#244](https://github.com/github-api/github-api/pull/244) ([benbek](https://github.com/benbek))
- Support for auto\_init [\#240](https://github.com/github-api/github-api/pull/240) ([dlovera](https://github.com/dlovera))

## [github-api-1.72](https://github.com/github-api/github-api/tree/github-api-1.72) (2015-12-10)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.71...github-api-1.72)

**Fixed bugs:**

- GHRepository.getCompare\(GHBranch, GHBranch\) does not allow for cross-fork compares [\#218](https://github.com/github-api/github-api/issues/218)

**Closed issues:**

- Access to raw JSON for issues? [\#235](https://github.com/github-api/github-api/issues/235)
- GHRepository.getPullRequests\(\) / listPullRequests\(\) does not support the sort parameter [\#234](https://github.com/github-api/github-api/issues/234)
- Commit obtained by queryCommits does not contain files [\#230](https://github.com/github-api/github-api/issues/230)
- get starred projects by the user [\#228](https://github.com/github-api/github-api/issues/228)
- update of file in github [\#227](https://github.com/github-api/github-api/issues/227)
- Add per\_page paramter to the search builders [\#221](https://github.com/github-api/github-api/issues/221)
- RateLimitHandler Bug [\#220](https://github.com/github-api/github-api/issues/220)
- Followers and following pagination [\#213](https://github.com/github-api/github-api/issues/213)
- Some features of this plugin no longer work with the recent changes to api.github.com [\#202](https://github.com/github-api/github-api/issues/202)
- Add an option to get the id of an event [\#199](https://github.com/github-api/github-api/issues/199)
- Need documentation for how to clone a git repo to the disk [\#196](https://github.com/github-api/github-api/issues/196)
- JDK Version [\#188](https://github.com/github-api/github-api/issues/188)
- Obtain Pushed Commit using GitHub API [\#186](https://github.com/github-api/github-api/issues/186)
- @WithBridgeMethods decorator in GHObject has no value adapterMethod [\#171](https://github.com/github-api/github-api/issues/171)

## [github-api-1.71](https://github.com/github-api/github-api/tree/github-api-1.71) (2015-12-01)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.70...github-api-1.71)

**Fixed bugs:**

- \#218 enable cross fork compare [\#219](https://github.com/github-api/github-api/pull/219) ([if6was9](https://github.com/if6was9))
- \#215 fix read\(\) failure with private repos [\#216](https://github.com/github-api/github-api/pull/216) ([if6was9](https://github.com/if6was9))

**Closed issues:**

- Push to repo [\#229](https://github.com/github-api/github-api/issues/229)
- Can not find the .github file [\#223](https://github.com/github-api/github-api/issues/223)
- Can not find the .github file [\#222](https://github.com/github-api/github-api/issues/222)
- GHContent.read\(\) is broken due to incorrect HTTP Method [\#215](https://github.com/github-api/github-api/issues/215)

**Merged pull requests:**

- Use default timeouts for URLConnections [\#237](https://github.com/github-api/github-api/pull/237) ([olivergondza](https://github.com/olivergondza))
- Findbugs plugin has been upgraded [\#236](https://github.com/github-api/github-api/pull/236) ([recena](https://github.com/recena))
- Add information about mirror url if it exist. [\#233](https://github.com/github-api/github-api/pull/233) ([vparfonov](https://github.com/vparfonov))
- Added a new method to validate the GitHub API URL [\#232](https://github.com/github-api/github-api/pull/232) ([recena](https://github.com/recena))
- Support for merge\_commit\_sha [\#231](https://github.com/github-api/github-api/pull/231) ([recena](https://github.com/recena))
- Check builder result to either be a token or a user [\#226](https://github.com/github-api/github-api/pull/226) ([Shredder121](https://github.com/Shredder121))
- Overzealous FindBugs changes. [\#225](https://github.com/github-api/github-api/pull/225) ([Shredder121](https://github.com/Shredder121))
- Remove trailing slash when requesting directory content [\#224](https://github.com/github-api/github-api/pull/224) ([Shredder121](https://github.com/Shredder121))
- Support Milestone closed\_at date [\#217](https://github.com/github-api/github-api/pull/217) ([dblevins](https://github.com/dblevins))

## [github-api-1.70](https://github.com/github-api/github-api/tree/github-api-1.70) (2015-08-15)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.69...github-api-1.70)

**Closed issues:**

- How to search all repos based on a keyword? [\#211](https://github.com/github-api/github-api/issues/211)
- Missing: List private organizations a user belongs [\#209](https://github.com/github-api/github-api/issues/209)

**Merged pull requests:**

- Added option to edit GitHub release once it is created [\#212](https://github.com/github-api/github-api/pull/212) ([umajeric](https://github.com/umajeric))
- Cleanup issues discovered by FindBugs [\#210](https://github.com/github-api/github-api/pull/210) ([oleg-nenashev](https://github.com/oleg-nenashev))

## [github-api-1.69](https://github.com/github-api/github-api/tree/github-api-1.69) (2015-07-17)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.68...github-api-1.69)

**Closed issues:**

- NullPointerException in Requester.java [\#194](https://github.com/github-api/github-api/issues/194)
- Dependency problems [\#191](https://github.com/github-api/github-api/issues/191)
- Enable this to work with GitHub Enterprise [\#184](https://github.com/github-api/github-api/issues/184)
- no way to list forks of a repository [\#183](https://github.com/github-api/github-api/issues/183)
- Nothing is Fetched when calling repo.listNotifications\(\); [\#181](https://github.com/github-api/github-api/issues/181)
- java.net.ProtocolException: DELETE does not support writing [\#180](https://github.com/github-api/github-api/issues/180)

**Merged pull requests:**

- Fix potential NPE in the code [\#208](https://github.com/github-api/github-api/pull/208) ([oleg-nenashev](https://github.com/oleg-nenashev))
- Enable FindBugs in the repo [\#207](https://github.com/github-api/github-api/pull/207) ([oleg-nenashev](https://github.com/oleg-nenashev))
- Specified the GET [\#206](https://github.com/github-api/github-api/pull/206) ([torodev](https://github.com/torodev))
- Fix NPE found when resolving issues from search api [\#205](https://github.com/github-api/github-api/pull/205) ([stephenc](https://github.com/stephenc))
- add ping event to GH events enum [\#204](https://github.com/github-api/github-api/pull/204) ([lanwen](https://github.com/lanwen))
- GitHub API have changed the semantics of /user/repos API [\#203](https://github.com/github-api/github-api/pull/203) ([lucamilanesio](https://github.com/lucamilanesio))
- Add support for update/delete operations on issue comments [\#201](https://github.com/github-api/github-api/pull/201) ([henryju](https://github.com/henryju))
- fix for unused json map when method with body, but body is null [\#200](https://github.com/github-api/github-api/pull/200) ([lanwen](https://github.com/lanwen))
- fix for GH Enterprise which does not have rate limit reset field [\#198](https://github.com/github-api/github-api/pull/198) ([lanwen](https://github.com/lanwen))
- added Page Build [\#197](https://github.com/github-api/github-api/pull/197) ([treeduck](https://github.com/treeduck))
- Enable creation and retrieval of org webhooks [\#192](https://github.com/github-api/github-api/pull/192) ([chrisrhut](https://github.com/chrisrhut))
- allow default branch to be set [\#190](https://github.com/github-api/github-api/pull/190) ([if6was9](https://github.com/if6was9))
- fixed regression that caused POST operations to be sent as GET [\#189](https://github.com/github-api/github-api/pull/189) ([if6was9](https://github.com/if6was9))
- Recognize previous\_file field in GHCommit.File [\#187](https://github.com/github-api/github-api/pull/187) ([yegorius](https://github.com/yegorius))
- Fixes \#183: added a method listForks\(\) to GHRepository [\#185](https://github.com/github-api/github-api/pull/185) ([marc-guenther](https://github.com/marc-guenther))
- Fix invalid URL for pull request comments update/delete [\#182](https://github.com/github-api/github-api/pull/182) ([henryju](https://github.com/henryju))

## [github-api-1.68](https://github.com/github-api/github-api/tree/github-api-1.68) (2015-04-20)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.67...github-api-1.68)

**Closed issues:**

- Merging with SHA1 [\#176](https://github.com/github-api/github-api/issues/176)

**Merged pull requests:**

- Fix NullPointerException on RateLimitHandler when handling API errors. [\#179](https://github.com/github-api/github-api/pull/179) ([lskillen](https://github.com/lskillen))

## [github-api-1.67](https://github.com/github-api/github-api/tree/github-api-1.67) (2015-04-14)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.66...github-api-1.67)

**Merged pull requests:**

- Added getters for the objects notifications refer to [\#177](https://github.com/github-api/github-api/pull/177) ([syniuhin](https://github.com/syniuhin))
- Added the source attribute to GHRepository [\#175](https://github.com/github-api/github-api/pull/175) ([kickroot](https://github.com/kickroot))
- Improvements [\#170](https://github.com/github-api/github-api/pull/170) ([KostyaSha](https://github.com/KostyaSha))
- Throw error for bad creds [\#169](https://github.com/github-api/github-api/pull/169) ([KostyaSha](https://github.com/KostyaSha))

## [github-api-1.66](https://github.com/github-api/github-api/tree/github-api-1.66) (2015-03-24)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.64...github-api-1.66)

**Closed issues:**

- Rate limiting causes silent freezing failures [\#172](https://github.com/github-api/github-api/issues/172)
- Pluggable persistent cache support [\#168](https://github.com/github-api/github-api/issues/168)
- Implement /search [\#158](https://github.com/github-api/github-api/issues/158)
- Support notifications api [\#119](https://github.com/github-api/github-api/issues/119)
- Consider committing to using OkHttp in preference to HttpURLConnection [\#104](https://github.com/github-api/github-api/issues/104)

## [github-api-1.64](https://github.com/github-api/github-api/tree/github-api-1.64) (2015-03-22)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.63...github-api-1.64)

**Closed issues:**

- SBT build is broken from version 1.53 [\#167](https://github.com/github-api/github-api/issues/167)
- Reading a gist in anonymonous mode causes error [\#166](https://github.com/github-api/github-api/issues/166)
- Add support for the Markdown API [\#165](https://github.com/github-api/github-api/issues/165)
- GHContent\#content always returns master version [\#162](https://github.com/github-api/github-api/issues/162)
- infinite Thread usage loop with handleApiError [\#159](https://github.com/github-api/github-api/issues/159)
- /repositories? [\#157](https://github.com/github-api/github-api/issues/157)
- 502 Bad Gateway error from GHRelease.uploadAsset [\#135](https://github.com/github-api/github-api/issues/135)

**Merged pull requests:**

- Add method to get the list of languages using in repository [\#161](https://github.com/github-api/github-api/pull/161) ([khoa-nd](https://github.com/khoa-nd))
- Picking endpoint from the properties file and environment variables [\#156](https://github.com/github-api/github-api/pull/156) ([ashwanthkumar](https://github.com/ashwanthkumar))
- Implementing github trees [\#155](https://github.com/github-api/github-api/pull/155) ([ddtxra](https://github.com/ddtxra))

## [github-api-1.63](https://github.com/github-api/github-api/tree/github-api-1.63) (2015-03-02)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.62...github-api-1.63)

**Closed issues:**

- Github Trees support [\#153](https://github.com/github-api/github-api/issues/153)
- getEvents fails periodically with odd exception [\#92](https://github.com/github-api/github-api/issues/92)

## [github-api-1.62](https://github.com/github-api/github-api/tree/github-api-1.62) (2015-02-15)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.61...github-api-1.62)

**Closed issues:**

- "Stars" and "Forks" parameters for Gist [\#154](https://github.com/github-api/github-api/issues/154)
- NPE during tag.getCommit\(\).getLastStatus\(\) [\#152](https://github.com/github-api/github-api/issues/152)
- Incorrect behavior of GHRepository.getReadme [\#150](https://github.com/github-api/github-api/issues/150)
- Make public GHRepository.getOwnerName [\#149](https://github.com/github-api/github-api/issues/149)
- Add information about thread-safety in Javadoc [\#148](https://github.com/github-api/github-api/issues/148)
- Add API to retrieve list of contributors [\#147](https://github.com/github-api/github-api/issues/147)
- Parsing a push event payload doesn't get the repository [\#144](https://github.com/github-api/github-api/issues/144)
- GHRelease issue [\#138](https://github.com/github-api/github-api/issues/138)
- GHRepository.getIssues\(GHIssueState.CLOSED\) also return pull requests [\#134](https://github.com/github-api/github-api/issues/134)
- Feature: watched Repositories [\#130](https://github.com/github-api/github-api/issues/130)
- Cannot create repository in organisation [\#118](https://github.com/github-api/github-api/issues/118)
- Different ways of getting issue.getClosedby\(\) return different results. [\#113](https://github.com/github-api/github-api/issues/113)
- NullPointerException in GHPerson [\#111](https://github.com/github-api/github-api/issues/111)
- Suggested enhancement: GHPerson\#getAllRepositories\(\) [\#110](https://github.com/github-api/github-api/issues/110)
- add support for proxy [\#109](https://github.com/github-api/github-api/issues/109)
- Error while accessing rate limit API -  No subject alternative DNS name matching api.github.com found. [\#108](https://github.com/github-api/github-api/issues/108)
- Add support for retrieving repository available labels [\#105](https://github.com/github-api/github-api/issues/105)
- getReadme its outdated [\#99](https://github.com/github-api/github-api/issues/99)

## [github-api-1.61](https://github.com/github-api/github-api/tree/github-api-1.61) (2015-02-14)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.60...github-api-1.61)

## [github-api-1.60](https://github.com/github-api/github-api/tree/github-api-1.60) (2015-02-14)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.59...github-api-1.60)

**Closed issues:**

- GHTeam.getMembers\(\) response does not include all users if user count \>30 [\#145](https://github.com/github-api/github-api/issues/145)

**Merged pull requests:**

- fix \#145 GHTeam.getMembers\(\) does not page properly [\#146](https://github.com/github-api/github-api/pull/146) ([if6was9](https://github.com/if6was9))
- Complete api implementation for the new github deployment api [\#143](https://github.com/github-api/github-api/pull/143) ([suryagaddipati](https://github.com/suryagaddipati))
- Add code for creating deployments for a repo [\#142](https://github.com/github-api/github-api/pull/142) ([suryagaddipati](https://github.com/suryagaddipati))
- Trivial change to enable creating/updating binary content \(files\). [\#141](https://github.com/github-api/github-api/pull/141) ([alvaro1728](https://github.com/alvaro1728))
- Put mockito in the test scope. [\#139](https://github.com/github-api/github-api/pull/139) ([farmdawgnation](https://github.com/farmdawgnation))
- added 'diverged' constant to GHCompare.Status enum [\#137](https://github.com/github-api/github-api/pull/137) ([simonecarriero](https://github.com/simonecarriero))
- Add paging support for Team's Repositories [\#136](https://github.com/github-api/github-api/pull/136) ([rtyley](https://github.com/rtyley))

## [github-api-1.59](https://github.com/github-api/github-api/tree/github-api-1.59) (2014-10-08)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.58...github-api-1.59)

**Merged pull requests:**

- Add GHCompare.getFiles\(\) method to be able to see the precise files chan... [\#132](https://github.com/github-api/github-api/pull/132) ([mocleiri](https://github.com/mocleiri))
- Modify GitHubBuilder to resolve user credentials from the system environ... [\#131](https://github.com/github-api/github-api/pull/131) ([mocleiri](https://github.com/mocleiri))
- Allow pullRequest.getHead\(\).getRepository\(\).getCommit\(headSha1\) to work [\#129](https://github.com/github-api/github-api/pull/129) ([mocleiri](https://github.com/mocleiri))
- Update github scopes according to https://developer.github.com/v3/oauth/\#scopes [\#128](https://github.com/github-api/github-api/pull/128) ([ndeloof](https://github.com/ndeloof))
- Allow to use custom HttpConnector when only OAuth token is given [\#124](https://github.com/github-api/github-api/pull/124) ([ohtake](https://github.com/ohtake))
- Use issues endpoints for pull requests [\#123](https://github.com/github-api/github-api/pull/123) ([rtyley](https://github.com/rtyley))
- Add missing field browser\_download\_url in GHAsset [\#122](https://github.com/github-api/github-api/pull/122) ([tbruyelle](https://github.com/tbruyelle))

## [github-api-1.58](https://github.com/github-api/github-api/tree/github-api-1.58) (2014-08-30)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.57...github-api-1.58)

**Merged pull requests:**

- All the refs worth knowing: Implementation of ref updating and deleting. [\#117](https://github.com/github-api/github-api/pull/117) ([farmdawgnation](https://github.com/farmdawgnation))
- Remove getPath\(\) [\#116](https://github.com/github-api/github-api/pull/116) ([DavidTanner](https://github.com/DavidTanner))
- Add missing GitHub event types. [\#115](https://github.com/github-api/github-api/pull/115) ([bernd](https://github.com/bernd))
- get repository full name \(including owner\) [\#114](https://github.com/github-api/github-api/pull/114) ([ndeloof](https://github.com/ndeloof))
- General pagination [\#107](https://github.com/github-api/github-api/pull/107) ([msperisen](https://github.com/msperisen))

## [github-api-1.57](https://github.com/github-api/github-api/tree/github-api-1.57) (2014-08-19)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.56...github-api-1.57)

**Merged pull requests:**

- Get all orgs/teams/permissions in a single GitHub API call [\#112](https://github.com/github-api/github-api/pull/112) ([lucamilanesio](https://github.com/lucamilanesio))
- Implement pagination on list of private+public repos of a user. [\#106](https://github.com/github-api/github-api/pull/106) ([lucamilanesio](https://github.com/lucamilanesio))

## [github-api-1.56](https://github.com/github-api/github-api/tree/github-api-1.56) (2014-07-03)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.55...github-api-1.56)

**Closed issues:**

- Unable to access commit date through api. [\#100](https://github.com/github-api/github-api/issues/100)
- Add support for commit status contexts. [\#96](https://github.com/github-api/github-api/issues/96)

**Merged pull requests:**

- Update to OkHttp 2.0.0, which has a new OkUrlFactory [\#103](https://github.com/github-api/github-api/pull/103) ([rtyley](https://github.com/rtyley))
- Better FNFE from delete\(\) [\#102](https://github.com/github-api/github-api/pull/102) ([jglick](https://github.com/jglick))
- Un-finalize a handful of classes. [\#101](https://github.com/github-api/github-api/pull/101) ([farmdawgnation](https://github.com/farmdawgnation))

## [github-api-1.55](https://github.com/github-api/github-api/tree/github-api-1.55) (2014-06-08)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.54...github-api-1.55)

**Merged pull requests:**

- Add support for adding context to commit status. [\#97](https://github.com/github-api/github-api/pull/97) ([suryagaddipati](https://github.com/suryagaddipati))

## [github-api-1.54](https://github.com/github-api/github-api/tree/github-api-1.54) (2014-06-05)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.53...github-api-1.54)

**Closed issues:**

- Version 1.8 of bridge-method-annotation not available in maven central [\#91](https://github.com/github-api/github-api/issues/91)
- Ability to specify both branch and sha parameters at same time in GHCommitQueryBuilder [\#90](https://github.com/github-api/github-api/issues/90)

**Merged pull requests:**

- Add support for retriving a single ref [\#95](https://github.com/github-api/github-api/pull/95) ([suryagaddipati](https://github.com/suryagaddipati))
- Add support for adding deploykeys to repo [\#94](https://github.com/github-api/github-api/pull/94) ([suryagaddipati](https://github.com/suryagaddipati))
- Upgrading to 1.12 version for bridge-method-annotation and bridge-method-injector - fix for \#91 [\#93](https://github.com/github-api/github-api/pull/93) ([vr100](https://github.com/vr100))

## [github-api-1.53](https://github.com/github-api/github-api/tree/github-api-1.53) (2014-05-10)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.52...github-api-1.53)

**Closed issues:**

- GHUser.getRepositories\(\) does not pull private repositories [\#88](https://github.com/github-api/github-api/issues/88)
- create pull requests? [\#79](https://github.com/github-api/github-api/issues/79)
- getRateLimit\(\) fails for GitHub Enterprise [\#78](https://github.com/github-api/github-api/issues/78)
- GHRepository assumes public github.com, won't work with github enterprise [\#64](https://github.com/github-api/github-api/issues/64)
- Getting private repositories for a private organization [\#62](https://github.com/github-api/github-api/issues/62)
- ClosedBy returns nothing for closed issues [\#60](https://github.com/github-api/github-api/issues/60)
- class file for com.infradna.tool.bridge\_method\_injector.WithBridgeMethods not found [\#54](https://github.com/github-api/github-api/issues/54)
- add support of Gists [\#53](https://github.com/github-api/github-api/issues/53)
- GHUser is missing a getHTMLURL\(\) method [\#52](https://github.com/github-api/github-api/issues/52)
- \[Feature Request\] get tags [\#40](https://github.com/github-api/github-api/issues/40)
- GitHub.connectAnonymously\(\) fails because of a lack of credentials. [\#39](https://github.com/github-api/github-api/issues/39)

**Merged pull requests:**

- create a Release & Branch [\#84](https://github.com/github-api/github-api/pull/84) ([fanfansama](https://github.com/fanfansama))

## [github-api-1.52](https://github.com/github-api/github-api/tree/github-api-1.52) (2014-05-10)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.51...github-api-1.52)

**Closed issues:**

- File size limited to 1MB [\#85](https://github.com/github-api/github-api/issues/85)

**Merged pull requests:**

- Fix bug in GHMyself.getEmails due to API change [\#87](https://github.com/github-api/github-api/pull/87) ([kellycampbell](https://github.com/kellycampbell))
- Using builder pattern to list commits in a repo by author, branch, etc [\#86](https://github.com/github-api/github-api/pull/86) ([vr100](https://github.com/vr100))
- add tarball\_url and zipball\_url to GHRelease [\#83](https://github.com/github-api/github-api/pull/83) ([antonkrasov](https://github.com/antonkrasov))

## [github-api-1.51](https://github.com/github-api/github-api/tree/github-api-1.51) (2014-04-13)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.50...github-api-1.51)

**Closed issues:**

- Add support for setting explicit connection timeouts [\#81](https://github.com/github-api/github-api/issues/81)
- Throwing an Error when an IOException occurs is overly fatal [\#65](https://github.com/github-api/github-api/issues/65)

**Merged pull requests:**

- Cast url.openConnection\(\) to HttpURLConnection instead of HttpsURLConnec... [\#82](https://github.com/github-api/github-api/pull/82) ([prazanna](https://github.com/prazanna))
- Add support for removing a user from an Organisation [\#80](https://github.com/github-api/github-api/pull/80) ([rtyley](https://github.com/rtyley))

## [github-api-1.50](https://github.com/github-api/github-api/tree/github-api-1.50) (2014-03-28)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.49...github-api-1.50)

**Closed issues:**

- publish 1.49 version to jenkins maven repo [\#63](https://github.com/github-api/github-api/issues/63)

**Merged pull requests:**

- Add org public-members call, to complement the full members list [\#76](https://github.com/github-api/github-api/pull/76) ([rtyley](https://github.com/rtyley))
- Support the check-user-team-membership API call [\#75](https://github.com/github-api/github-api/pull/75) ([rtyley](https://github.com/rtyley))
- Fix GHIssue.setLabels\(\) [\#73](https://github.com/github-api/github-api/pull/73) ([rtyley](https://github.com/rtyley))
- Un-finalize GHContent. [\#72](https://github.com/github-api/github-api/pull/72) ([farmdawgnation](https://github.com/farmdawgnation))
- Created new method to automate the merge [\#69](https://github.com/github-api/github-api/pull/69) ([vanjikumaran](https://github.com/vanjikumaran))
- Enable org member filtering [\#68](https://github.com/github-api/github-api/pull/68) ([lindseydew](https://github.com/lindseydew))
- Support paging when fetching organization members [\#66](https://github.com/github-api/github-api/pull/66) ([ryankennedy](https://github.com/ryankennedy))

## [github-api-1.49](https://github.com/github-api/github-api/tree/github-api-1.49) (2014-01-06)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.48...github-api-1.49)

## [github-api-1.48](https://github.com/github-api/github-api/tree/github-api-1.48) (2014-01-06)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.47...github-api-1.48)

**Closed issues:**

- Implement Contents API [\#46](https://github.com/github-api/github-api/issues/46)

**Merged pull requests:**

- Fetching of user's verified keys through standard OAuth scope. [\#61](https://github.com/github-api/github-api/pull/61) ([lucamilanesio](https://github.com/lucamilanesio))
- Contents API [\#59](https://github.com/github-api/github-api/pull/59) ([farmdawgnation](https://github.com/farmdawgnation))

## [github-api-1.47](https://github.com/github-api/github-api/tree/github-api-1.47) (2013-11-27)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.46...github-api-1.47)

**Closed issues:**

- github /user/orgs fails [\#57](https://github.com/github-api/github-api/issues/57)
- GHRepository.getIssues\(\) limited to 30 issues [\#55](https://github.com/github-api/github-api/issues/55)

**Merged pull requests:**

- Add support for PULL\_REQUEST\_REVIEW\_COMMENT event types. [\#58](https://github.com/github-api/github-api/pull/58) ([rtholmes](https://github.com/rtholmes))
- Use `PagedIterator\<GHIssue\>` to retrieve repository issues [\#56](https://github.com/github-api/github-api/pull/56) ([endeavor85](https://github.com/endeavor85))

## [github-api-1.46](https://github.com/github-api/github-api/tree/github-api-1.46) (2013-11-13)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.45...github-api-1.46)

## [github-api-1.45](https://github.com/github-api/github-api/tree/github-api-1.45) (2013-11-09)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.44...github-api-1.45)

**Closed issues:**

- Issue labels have multiple fields now [\#48](https://github.com/github-api/github-api/issues/48)

**Merged pull requests:**

- add support \(most of\) the release-related endpoints [\#51](https://github.com/github-api/github-api/pull/51) ([evanchooly](https://github.com/evanchooly))
- Updates Jackson to 2.2.3 [\#50](https://github.com/github-api/github-api/pull/50) ([pescuma](https://github.com/pescuma))
- Use a proper Label in GHIssues [\#49](https://github.com/github-api/github-api/pull/49) ([pescuma](https://github.com/pescuma))
- Allows to define page size for repository lists and other API enhancements [\#45](https://github.com/github-api/github-api/pull/45) ([lucamilanesio](https://github.com/lucamilanesio))

## [github-api-1.44](https://github.com/github-api/github-api/tree/github-api-1.44) (2013-09-07)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.43...github-api-1.44)

**Closed issues:**

- getMergeableState in GHPullRequest doesn't work [\#41](https://github.com/github-api/github-api/issues/41)

**Merged pull requests:**

- GHMyself should allow accessing the private repos and orgs too [\#43](https://github.com/github-api/github-api/pull/43) ([stephenc](https://github.com/stephenc))
- Commit's short info model [\#42](https://github.com/github-api/github-api/pull/42) ([paulbutenko](https://github.com/paulbutenko))

## [github-api-1.43](https://github.com/github-api/github-api/tree/github-api-1.43) (2013-07-07)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.42...github-api-1.43)

## [github-api-1.42](https://github.com/github-api/github-api/tree/github-api-1.42) (2013-05-07)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.41...github-api-1.42)

**Merged pull requests:**

- add repository to Pull Request payload and wrap the PR with the repository [\#38](https://github.com/github-api/github-api/pull/38) ([janinko](https://github.com/janinko))
- Force issues-based API route for PR comments [\#37](https://github.com/github-api/github-api/pull/37) ([spiffxp](https://github.com/spiffxp))
- Allow oauthToken to be used without login [\#36](https://github.com/github-api/github-api/pull/36) ([spiffxp](https://github.com/spiffxp))

## [github-api-1.41](https://github.com/github-api/github-api/tree/github-api-1.41) (2013-04-23)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.40...github-api-1.41)

**Closed issues:**

- Closing pull request using Github API return FileNotFoundException [\#34](https://github.com/github-api/github-api/issues/34)

**Merged pull requests:**

- Stop using deprecated API tokens for Enterprise [\#33](https://github.com/github-api/github-api/pull/33) ([watsonian](https://github.com/watsonian))

## [github-api-1.40](https://github.com/github-api/github-api/tree/github-api-1.40) (2013-04-16)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.39...github-api-1.40)

## [github-api-1.39](https://github.com/github-api/github-api/tree/github-api-1.39) (2013-04-16)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.38...github-api-1.39)

**Merged pull requests:**

- Provide a way to determine if the connection is anonymous [\#44](https://github.com/github-api/github-api/pull/44) ([stephenc](https://github.com/stephenc))
- Implement GHEventPayload.IssueComment [\#32](https://github.com/github-api/github-api/pull/32) ([janinko](https://github.com/janinko))
- implement retrieving of access token [\#31](https://github.com/github-api/github-api/pull/31) ([janinko](https://github.com/janinko))

## [github-api-1.38](https://github.com/github-api/github-api/tree/github-api-1.38) (2013-04-07)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.37...github-api-1.38)

**Closed issues:**

- Error 500 - No Protocol [\#29](https://github.com/github-api/github-api/issues/29)

## [github-api-1.37](https://github.com/github-api/github-api/tree/github-api-1.37) (2013-03-15)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.36...github-api-1.37)

**Merged pull requests:**

- Adding Compare and Refs commands to API [\#30](https://github.com/github-api/github-api/pull/30) ([mc1arke](https://github.com/mc1arke))

## [github-api-1.36](https://github.com/github-api/github-api/tree/github-api-1.36) (2013-01-24)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.35...github-api-1.36)

## [github-api-1.35](https://github.com/github-api/github-api/tree/github-api-1.35) (2013-01-07)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.34...github-api-1.35)

**Closed issues:**

- Support for organization check mebership [\#23](https://github.com/github-api/github-api/issues/23)

**Merged pull requests:**

- Password is no longer required for api usage and fix for broken base64 encoding. [\#27](https://github.com/github-api/github-api/pull/27) ([johnou](https://github.com/johnou))
- Removed web client and proprietary api usage. [\#26](https://github.com/github-api/github-api/pull/26) ([johnou](https://github.com/johnou))

## [github-api-1.34](https://github.com/github-api/github-api/tree/github-api-1.34) (2013-01-06)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.33...github-api-1.34)

**Closed issues:**

- Enterprise Github without HTTPS not supported [\#12](https://github.com/github-api/github-api/issues/12)

**Merged pull requests:**

- JENKINS-13726: Github plugin should work with Github enterprise by allowing for overriding the github URL. [\#24](https://github.com/github-api/github-api/pull/24) ([johnou](https://github.com/johnou))
- Retrieve repository directly. [\#22](https://github.com/github-api/github-api/pull/22) ([janinko](https://github.com/janinko))

## [github-api-1.33](https://github.com/github-api/github-api/tree/github-api-1.33) (2012-09-13)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.32...github-api-1.33)

**Closed issues:**

- GHIssue.getComments\(\) throws NoSuchElementException when there are no comments [\#20](https://github.com/github-api/github-api/issues/20)
- scm in pom.xml is incorrect [\#14](https://github.com/github-api/github-api/issues/14)
- support for: Create an issue  POST /repos/:user/:repo/issues [\#13](https://github.com/github-api/github-api/issues/13)

**Merged pull requests:**

- PagedIterable dosn't use authentication [\#19](https://github.com/github-api/github-api/pull/19) ([janinko](https://github.com/janinko))
- When using lazy population, this is not deprecated [\#18](https://github.com/github-api/github-api/pull/18) ([janinko](https://github.com/janinko))

## [github-api-1.32](https://github.com/github-api/github-api/tree/github-api-1.32) (2012-09-06)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.31...github-api-1.32)

**Merged pull requests:**

- Issues pull requests apiv3 [\#17](https://github.com/github-api/github-api/pull/17) ([janinko](https://github.com/janinko))

## [github-api-1.31](https://github.com/github-api/github-api/tree/github-api-1.31) (2012-08-28)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.30...github-api-1.31)

**Merged pull requests:**

- Fixes for github api v3 [\#16](https://github.com/github-api/github-api/pull/16) ([janinko](https://github.com/janinko))

## [github-api-1.30](https://github.com/github-api/github-api/tree/github-api-1.30) (2012-08-28)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.29...github-api-1.30)

**Merged pull requests:**

- Using pagination when getting Pull Requests from a repository [\#15](https://github.com/github-api/github-api/pull/15) ([athieriot](https://github.com/athieriot))

## [github-api-1.29](https://github.com/github-api/github-api/tree/github-api-1.29) (2012-06-18)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.28...github-api-1.29)

**Closed issues:**

- NPE Crash on 1.27 [\#11](https://github.com/github-api/github-api/issues/11)
- Github API V2 shuts down [\#8](https://github.com/github-api/github-api/issues/8)

## [github-api-1.28](https://github.com/github-api/github-api/tree/github-api-1.28) (2012-06-13)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.27...github-api-1.28)

## [github-api-1.27](https://github.com/github-api/github-api/tree/github-api-1.27) (2012-06-12)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.26...github-api-1.27)

## [github-api-1.26](https://github.com/github-api/github-api/tree/github-api-1.26) (2012-06-04)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.25...github-api-1.26)

## [github-api-1.25](https://github.com/github-api/github-api/tree/github-api-1.25) (2012-05-22)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.24...github-api-1.25)

## [github-api-1.24](https://github.com/github-api/github-api/tree/github-api-1.24) (2012-05-22)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.23...github-api-1.24)

## [github-api-1.23](https://github.com/github-api/github-api/tree/github-api-1.23) (2012-04-25)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.22...github-api-1.23)

## [github-api-1.22](https://github.com/github-api/github-api/tree/github-api-1.22) (2012-04-12)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.21...github-api-1.22)

## [github-api-1.21](https://github.com/github-api/github-api/tree/github-api-1.21) (2012-04-12)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.20...github-api-1.21)

**Closed issues:**

- Link to Javadoc incorrect at http://github-api.kohsuke.org/ [\#4](https://github.com/github-api/github-api/issues/4)

## [github-api-1.20](https://github.com/github-api/github-api/tree/github-api-1.20) (2012-04-11)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.19...github-api-1.20)

## [github-api-1.19](https://github.com/github-api/github-api/tree/github-api-1.19) (2012-04-06)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.18...github-api-1.19)

**Merged pull requests:**

- Listing of branches in a repository [\#7](https://github.com/github-api/github-api/pull/7) ([derfred](https://github.com/derfred))

## [github-api-1.18](https://github.com/github-api/github-api/tree/github-api-1.18) (2012-03-08)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.17...github-api-1.18)

**Merged pull requests:**

- milestone api via v3 [\#6](https://github.com/github-api/github-api/pull/6) ([YusukeKokubo](https://github.com/YusukeKokubo))

## [github-api-1.17](https://github.com/github-api/github-api/tree/github-api-1.17) (2012-02-12)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.16...github-api-1.17)

**Closed issues:**

- error on getRepositories\(\) [\#5](https://github.com/github-api/github-api/issues/5)

## [github-api-1.16](https://github.com/github-api/github-api/tree/github-api-1.16) (2012-01-03)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.15...github-api-1.16)

**Merged pull requests:**

- Fix for finding private repos on organizations [\#3](https://github.com/github-api/github-api/pull/3) ([jkrall](https://github.com/jkrall))

## [github-api-1.15](https://github.com/github-api/github-api/tree/github-api-1.15) (2012-01-01)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.14...github-api-1.15)

## [github-api-1.14](https://github.com/github-api/github-api/tree/github-api-1.14) (2011-10-27)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.13...github-api-1.14)

## [github-api-1.13](https://github.com/github-api/github-api/tree/github-api-1.13) (2011-09-15)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.12...github-api-1.13)

**Merged pull requests:**

- expose issue\_updated\_at. It looks like a better representation of update  [\#2](https://github.com/github-api/github-api/pull/2) ([lacostej](https://github.com/lacostej))

## [github-api-1.12](https://github.com/github-api/github-api/tree/github-api-1.12) (2011-08-27)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.11...github-api-1.12)

## [github-api-1.11](https://github.com/github-api/github-api/tree/github-api-1.11) (2011-08-27)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.10...github-api-1.11)

## [github-api-1.10](https://github.com/github-api/github-api/tree/github-api-1.10) (2011-07-11)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.9...github-api-1.10)

**Merged pull requests:**

- Add support for oauth token and a way to see my organizations [\#1](https://github.com/github-api/github-api/pull/1) ([mocleiri](https://github.com/mocleiri))

## [github-api-1.9](https://github.com/github-api/github-api/tree/github-api-1.9) (2011-06-28)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.8...github-api-1.9)

## [github-api-1.8](https://github.com/github-api/github-api/tree/github-api-1.8) (2011-06-17)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.7...github-api-1.8)

## [github-api-1.7](https://github.com/github-api/github-api/tree/github-api-1.7) (2011-05-28)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.6...github-api-1.7)

## [github-api-1.6](https://github.com/github-api/github-api/tree/github-api-1.6) (2011-03-16)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.5...github-api-1.6)

## [github-api-1.5](https://github.com/github-api/github-api/tree/github-api-1.5) (2011-02-23)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.4...github-api-1.5)

## [github-api-1.4](https://github.com/github-api/github-api/tree/github-api-1.4) (2010-12-14)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.3...github-api-1.4)

## [github-api-1.3](https://github.com/github-api/github-api/tree/github-api-1.3) (2010-11-24)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.2...github-api-1.3)

## [github-api-1.2](https://github.com/github-api/github-api/tree/github-api-1.2) (2010-04-19)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.1...github-api-1.2)

## [github-api-1.1](https://github.com/github-api/github-api/tree/github-api-1.1) (2010-04-19)

[Full Changelog](https://github.com/github-api/github-api/compare/github-api-1.0...github-api-1.1)

## [github-api-1.0](https://github.com/github-api/github-api/tree/github-api-1.0) (2010-04-19)

[Full Changelog](https://github.com/github-api/github-api/compare/ecbfdd7315ef2cf04b2be7f11a072ce0bd00c396...github-api-1.0)



\* *This Changelog was automatically generated by [github_changelog_generator](https://github.com/github-changelog-generator/github-changelog-generator)*
