# dwca-utils

Darwin Core Archive Utils for parsing archives that are compliant with the [Darwin Core Text Guide](http://rs.tdwg.org/dwc/terms/guides/text/)

[![Build Status](https://travis-ci.org/ansell/dwca-utils.svg?branch=master)](https://travis-ci.org/ansell/dwca-utils) [![Coverage Status](https://coveralls.io/repos/ansell/dwca-utils/badge.svg?branch=master)](https://coveralls.io/r/ansell/dwca-utils?branch=master)

# Setup

Install Maven and Git

Download the Git repository.

Set the relevant programs to be executable.

    chmod a+x ./dwcacheck
    chmod a+x ./csv2dwca

# Darwin Core Archive Checker

Checks that archives are compliant with the Darwin Core Text Guide

## Usage

Run dwcacheck with --help to get usage details:

    ./dwcacheck --help

# Darwin Core Metadata Generator

Generates Darwin Core Text metadata.xml files based on given core and extension files.

## Usage

Run csv2dwca with --help to get usage details:

    ./csv2dwca --help

# Maven

    <dependency>
        <groupId>com.github.ansell.dwca</groupId>
        <artifactId>dwca-utils</artifactId>
        <version>0.0.4</version>
    </dependency>

# Changelog

## 2017-10-17
* Release 0.0.4
* Add support for optional default value interpolation from meta.xml when iterating
* Add iteration over files
* Add support for specifying core id index when using csv2dwca

## 2017-08-21
* Parse ALA headings.csv files into Darwin Core Text Guide metadata XML files

## 2017-06-07
* Check extension files when using dwcacheck
* Pass quote/field separator/line ending information from metadata.xml through to CSV parser

## 2017-04-03
* Release 0.0.3
* Allow overriding of headers in source files during generation
* Allow the use of meta.xml field definitions during dwcacheck and integrate with csvsum to generate statistics using them

## 2016-05-04
* Release 0.0.2

## 2016-03-29
* Add --show-defaults to allow the printing of values that should be assumed from the spec, but are not always interpreted correctly by third-parties
* Support meta.xml along with metadata.xml for detection of the metadata file in zip files. Strange this is not standardised in the spec.

## 2016-03-24
* Add --extension to allow arbitrary number of extension files for the Metadata Generator in addition to a single Core file using --input

## 2016-03-23
* Release 0.0.1
* Complete the Metadata Generator 

## 2016-03-22
* Add stub for Darwin Core Metadata Generator to take CSV files and generate stub metadata.xml files
* Implement loading and some validation of metadata.xml documents

## 2016-03-21
* Initial commit
