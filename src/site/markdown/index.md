What is this?
=====

This library defines an object oriented representation of the GitHub API. By "object oriented" we mean
there are classes that correspond to the domain model of GitHub (such as `GHUser` and `GHRepository`),
operations that act on them as defined as methods (such as `GHUser.follow()`), and those object references
are used in favor of using string handle (such as `GHUser.isMemberOf(GHOrganization)` instead of
`GHUser.isMemberOf(String)`)

The library supports both github.com and GitHub Enterprise.

Most of the GitHub APIs are covered, although there are some corners that are still not yet implemented.

Sample Usage
-----

    GitHub github = GitHub.connect();
    GHRepository repo = github.createRepository(
      "new-repository","this is my new repository",
      "http://www.kohsuke.org/",true/*public*/);
    repo.addCollaborators(github.getUser("abayer"),github.getUser("rtyler"));
    repo.delete();

Credential
----

This library allows the caller to supply the credential as parameters, but it also defines a common convention
so that applications using this library will look at the consistent location. In this convention, the library
looks at `~/.github` property file, which should have the following two values:

    login=kohsuke
    password=012345678

Alternatively, you can have just the OAuth token in this file:

    oauth=4d98173f7c075527cb64878561d1fe70

OkHttp
----
This library comes with a pluggable connector to use different HTTP client implementations
through `HttpConnector`. In particular, this means you can use [OkHttp](http://square.github.io/okhttp/),
so we can make use of it's HTTP response cache.
Making a conditional request against the GitHub API and receiving a 304 response
[does not count against the rate limit](http://developer.github.com/v3/#conditional-requests).

The following code shows an example of how to set up persistent cache on the disk:

    Cache cache = new Cache(cacheDirectory, 10 * 1024 * 1024); // 10MB cache
    GitHub gitHub = GitHubBuilder.fromCredentials()
        .withConnector(new OkHttpConnector(new OkUrlFactory(new OkHttpClient().setCache(cache))))
        .build();
