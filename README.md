#RxKotlinFX/TornadoFX Demo

This is a fully-featured demo showcasing [TornadoFX](https://github.com/edvin/tornadofx) used in conjunction with [RxKotlinFX](https://github.com/thomasnield/RxKotlinFX) to build a desktop application. It only took me two nights to build. 

This is an [RxJava](https://github.com/ReactiveX/RxJava)-driven application. A temporary backing database is stored in-memory using [SQLite](https://www.sqlite.org/), and [RxJava-JDBC](https://github.com/davidmoten/rxjava-jdbc) is used to reactively query and write to it. 

Just build and run with Gradle.  
![](http://i.imgur.com/KMJQX9W.gif)

##Stack

Here is a list and Venn diagram of the different libraries used, with overlaps indicating their role in interoperability. 

* [RxJava](https://github.com/ReactiveX/RxJava)
* [RxKotlin](https://github.com/ReactiveX/RxKotlin)
* [JavaFX](http://docs.oracle.com/javase/8/javase-clienttechnologies.htm)
* [Kotlin](https://kotlinlang.org/)
* [TornadoFX](https://github.com/edvin/tornadofx)
* [RxJavaFX](https://github.com/ReactiveX/RxJavaFX)
* [RxKotlinFX](https://github.com/thomasnield/RxKotlinFX)
* [RxJava-JDBC](https://github.com/davidmoten/rxjava-jdbc) 
* [SQLite](https://github.com/xerial/sqlite-jdbc)


![](http://i.imgur.com/6wIrIf2.pngh)

