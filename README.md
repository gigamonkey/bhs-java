# Berkeley High Computer Science Java

This repo contains code used for running tests in my browser-based coding
environment. As of this writing (summer 2024) I’m in the process of extracting
duplicate code from the tests themselves into the test framework so everything
is a little bit in pieces at the moment.

The basic idea of this framework is to make it easy to write assignments and
assesments with various kinds of tests. Often that’s as simple as writing a
class containing reference implementations of a bunch of methods and then code
to generate test inputs for each method; the tests then generate test inputs and
compare the results of calling the student methods and the reference methods.
There’s some reflection jiggery pokery involved since the student classes don’t
necessary implement any interface because we want to be able to test their code
before they’ve written all the methods.

Another style of test, that becomes important when we get to the units on
writing classes (as opposed to individual methods) uses reflection to check that
the class the student has written has particular criteria, e.g. it is named the
right thing, it is `public`, it has a `main` method.

Sometimes these two kinds of tests are combined to test first that a class has
desired characteristics and then that the specific methods work the way they are
supposed to.
