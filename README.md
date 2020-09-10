# BPjs: A JavaScript-based Behavioral Programming Runtime.

This repository contains a javascript-based [BP](http://www.b-prog.org) library.

[![Build Status](https://travis-ci.org/bThink-BGU/BPjs.svg?branch=master)](https://travis-ci.org/bThink-BGU/BPjs)
[![Coverage Status](https://coveralls.io/repos/github/bThink-BGU/BPjs/badge.svg?branch=master)](https://coveralls.io/github/bThink-BGU/BPjs?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.bthink-bgu/BPjs/badge.png?style-plastic)](https://repo.maven.apache.org/maven2/com/github/bthink-bgu/BPjs/)
[![Documentation Status](http://readthedocs.org/projects/bpjs/badge/?version=master)](http://bpjs.readthedocs.io/en/master/)
[![JavaDocs](https://img.shields.io/badge/javadocs-browse-green.svg)](http://www.javadoc.io/doc/com.github.bthink-bgu/BPjs/)

#### License
* BPjs is open sourced under the [MIT license](http://www.opensource.org/licenses/mit-license.php). If you use it in a system, please provide
a link to this page somewhere in the documentation/system about section.
* BPjs uses the Mozilla Rhino JavaScript engine. Project page and source code can be found [here](https://developer.mozilla.org/en-US/docs/Mozilla/Projects/Rhino).

---

## Getting BPjs
* For Maven projects: Add BPjs as dependency. Note that the version number changes.

````xml
<dependencies>
    ...
    <dependency>
        <groupId>com.github.bthink-bgu</groupId>
        <artifactId>BPjs</artifactId>
        <version>0.10.5</version>
    </dependency>
    ...
</dependencies>
````

* Clone, fork, or download the [starting project](https://github.com/bThink-BGU/SampleBPjsProject).
* Download the `.jar` files directly from [Maven Central](https://repo.maven.apache.org/maven2/com/github/bthink-bgu/BPjs/).
* The project's [Google group](https://groups.google.com/forum/#!forum/bpjs)

## Documentation

* [Devoxx Belgium 2018 talk](https://www.youtube.com/watch?v=PW8VdWA0UcA) introducing Behavioral Programming and BPjs.
* Presentations: [Introduction](https://www.slideshare.net/MichaelBarSinai/introducing-bpjs-web)
                 [Deeper dive](https://www.slideshare.net/MichaelBarSinai/deep-dive-into-bpjs)
* [Tutorial and Reference](http://bpjs.readthedocs.io/en/develop/)
* [API Javadocs](http://www.javadoc.io/doc/com.github.bthink-bgu/BPjs/)

## Change Log for the BPjs Library.

### 2020-08-09
* :arrow_up: Updated to use Rhino 1.7.13
* :arrow_up: Updated to Java 11
* :sparkle: A new type of violation: hot run, which looks at hot runs of a subset of a b-program b-threads. 
    Previously, BPjs supported all b-threads ("hot system run") or just a single one ("hot b-thread run").
* :part_alternation_mark: Liveness verification parts now aligned semantically with current theory.
* :put_litter_in_its_place: Code and documentation clean-ups.

### 2020-04-22
* :arrow_up: Updated to use Rhino 1.7.12
* :arrow_up: :tada: Can use ES6 syntax, such as `let` and `const`.
    * No arrow functions yet, there are some issues with continuations and arrow functions.
* :arrow_up: Synchronization statements are created more efficiently.
* :bug: `ComposableEventSet` checks arguments on creation, so that it can't be instantiated with `null` events (this later causes NPEs).
* :bug: `PrioritizedBThread` ESS fixed ([#70](https://github.com/bThink-BGU/BPjs/issues/70).
* :put_litter_in_its_place: Various small code clean-ups.
* :put_litter_in_its_place: Removed extraneous dependencies from `pom.xml`

[Earlier Changes](changelog-2019.md)

Legend:
* :arrows_counterclockwise: Change
* :sparkles:New feature
* :part_alternation_mark: Refactor (turns out this sign is called "part alternation mark" and not "weird 'M'", so it fits).
* :put_litter_in_its_place: Deprecation
* :arrow_up: Upgrade
* :bug: Bug fix
