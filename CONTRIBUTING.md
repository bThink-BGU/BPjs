# Contributing to BPjs

Thank you for your interest in contributing to BPjs! We are open to contributions from everyone. You don't need permission to participate. Just jump in. If you have questions, please reach out using our [Google Group](https://groups.google.com/forum/#!forum/bpjs).

## Background

BPjs strives to serve the budding BP community by being a dependable, high-quality infrastructure library. This means that the _definition of "done"_ for BPjs is a bit more thorough than that of regular projects (see below). While this slows development a bit, it allows BPjs to be a dependable (and dare we say boring?), foundation people can work on to create exciting stuff.  

BPjs is used on various platforms, from embedded devices to university super-computers (but, admittedly, mostly on normal laptops). It is also used in various ways: b-program execution (both general and customized), analysis, verification, an all kinds of weird combinations of the above. As the field of BP evolves, we are happy to see even more usages.

This variety requires BPjs to be very extensible and modular. Before forking and altering BPjs' core, keep in mind that many customizations can be done by implementing a module (say, an `EventSelectionStrategy`) and passing it to the correct engine. Of course, if you create a module that might be useful to others (say, `ReallyUsefulEventSelectionStrategy`), consider publishing it as an independent project, or donating it to BPjs.

The variety of usages and platforms constraints BPjs from depending on many external libraries for runtime. In fact, currently BPjs only depends on Mozilla Rhino, for JavaScript execution. It is possible to add more dependencies, as long as they are small enough to fit on embedded, and deemed useful enough for everybody. That's a pretty high bar, though, so if your project requires many dependencies, consider creating a project that uses BPjs, rather than adding these dependencies to BPjs itself.

## Definition of DONE

An issue is considers *done* when it has been:

* Implemented (duh) 
* Tested: We maintain high test coverage rate. We seldom approve pull-requests that lower it.
* Documented: Depending on the issue, this might require no documentation (just a bug fix), low-level/JavaDoc documentation (e.g. a new method), or high-level documentation. The latter is done by updating the [Sphinx docs](docs).
* Mentioned in README.md: Just add a line in the appropriate list. This list makes recent changes more visible to the community.

## Development Guidelines

* Work on a feature branch - we have many contributors, so if everyone worked on `develop` all the time there would be too many merge conflicts.
* When done (see above definition), create a pull request. The core BPjs team will take it from there.
* BPjs is a combined work of many people over a few years now. Some never met, some come from different cultures, etc. Thus, please be kind, keep discussions polite, assume no malice, and don't assume everyone gets internal jokes etc. Except for BP-related jokes, which should be OK. We shouldn't _block_ these. 
