What is this?
=====

This library defines an object oriented representation of the GitHub API. By "object oriented" we mean
there are classes that correspond to the domain model of GitHub (such as `GHUser` and `GHRepository`),
operations that act on them as defined as methods (such as `GHUser.follow()`), and those object references
are used in favor of using string handle (such as `GHUser.isMemberOf(GHOrganization)` instead of
`GHUser.isMemberOf(String)`)

There are some corners of the GitHub API that's not yet implemented, but
the library is implemented with the right abstractions and libraries to make it very easy to improve the coverage.

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

