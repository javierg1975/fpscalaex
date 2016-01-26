#Answers to exercises from Functional Programming in Scala

Plus whatever other interesting examples or problems I might encounter

##Also included:

More stringent compilation options for `scalac`. Adds [WartRemover](https://github.com/puffnfresh/wartremover) and [Scalastyle](http://www.scalastyle.org/) plugins to *sbt*

###Goodies included in the sbt config
- Static code analysis.
- Automatic code reviews based on best practices.
- Automatic enforcement of standard coding style.
- A [more capable](https://github.com/paulp/sbt-extras) `sbt` runner
- Peace of mind.

###How to use
- scalac options
    - These will kick in by default when you compile. Nothing to do.
- [WartRemover](https://github.com/puffnfresh/wartremover)
    - Will kick in by default when building with _sbt_.
    - Do check out the [documentation](https://github.com/puffnfresh/wartremover) for more options. Different projects might have slightly different needs.
- [Scalastyle](http://www.scalastyle.org/)
    - Run `sbt scalastyle`
    - Scalastyle allows the use of [rule suppression comments](http://stackoverflow.com/questions/21931431/how-can-i-suppress-scalastyle-warning). Learn how and when to use them
    - **DO NOT** follow advice blindly. Always try to understand the reasons behind a rule. Following advice blindly leads to fear. Fear leads to anger. Anger leads to hate. Hate leads to suffering (you get the point.)

###If you want to know more about the sbt project structure
Check out [Leif Wickland's presentation](http://confreaks.com/videos/4863-PNWS2014-towards-a-safer-scala) at PNWScala
