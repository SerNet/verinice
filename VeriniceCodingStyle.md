# Verinice Coding Style

This document defines basic principles, established by the Verinice team, for the coding style of
the Verinice project. All contributors and reviewers of Verinice source code shall aspire to follow
these principles as closely as possible to ensure the long term quality of the product.

## Basis

The basis for the Verinice Coding Style shall be these three documents:

* The [_Google Java Style_](https://google.github.io/styleguide/javaguide.html)
* The [Android project's _Code Style for Contributors_](https://source.android.com/source/code-style.html)
* _Effective Java (2nd edition)_ by Joshua Bloch

These three documents complement each other to a large extent. In some parts they overlap and in
very few aspects they contradict each other. Those few contradictions shall be clarified
unambiguously in this document. As probably not all of these potential contradictions are known at
the time of this writing, they shall become clarified as needed.

## Formatting

Consistent formatting is important. Amongst other reasons it avoids _diff_ pollution resulting from
reformatting.

### License Header

Every class and every interface file shall have a license header.

The license header shall comply to the following template, which in turn follows the
[instructions given by the Free Software Foundation](http://www.gnu.org/licenses/gpl-howto.html):

```java
/**
 * Copyright 2015 <author>.
 *
 * This file is part of Verinice.
 *
 * Verinice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Verinice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Verinice. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *  - <author> (<email>) - <contribution>
 */
```

### Indentation

* For each code block the indent increases by __four (4) spaces__.
* For wrapped lines the indent increases by __eight (8) spaces__.
* Tabs shall never be used.

The indentation spacing of four spaces is compliant with the Android project's _Code Style for
Contributors_, but contradictory to the _Google Java Style_.

### Line Length Limit

* Each line of code should be at most __100 characters__ long.

The same exceptions described in the [Android project's _Code Style for Contributors_](https://source.android.com/source/code-style.html) apply:

> * If a comment line contains an example command or a literal URL longer than 100 characters, that
>   line may be longer than 100 characters for ease of cut and paste.
> * Import lines can go over the limit because humans rarely see them (this also simplifies tool
>   writing).

## Exception Handling

Exceptions may never be ignored. All exception handling shall be compliant to _Chapter 9:
Exceptions_ in _Effective Java (2nd ed.)_.

### Abstraction Level of Exceptions

When handling checked exceptions thrown by third party APIs, care should be exercised to translate
them to the appropriate abstraction level as shown in the following example. This ensures that the
libraries providing the subsystem can be replaced without the need to adjust the exception handling
of the clients of the library (cf. _Effective Java (2nd ed._, Item 61)).

```java
interface ReportRepository {

    ReportTemplate getTemplateByName(String name) throws ReportRepositoryException;
}

class FileSystemReportRepository implements ReportRepository {

    ReportTemplate getTemplateByName(String name) throws ReportRepositoryException {
        try {
            return getTemplateFromFS(name);
        } catch(IOException e | FileNotFoundException e) {
            throw new ReportRepositoryException("fetching template " + name + " failed", e);
        }
    }
}

class DatabaseReportRepository implements ReportRepository {

  ReportTemplate getTemplateByName(String name) throws ReportRepositoryException {
     try {
       return getTemplateFromDatabase(name);
     } catch(SQLException e) {
       throw new ReportRepositoryException("fetching template " + name + " failed", e);
     }
  }
}
```

### Runtime Exceptions

If an exception indicates a situation that cannot be handled by the developer, it should get
translated into a runtime exception. This option should only be chosen as a last resort. When doing
this, the developer ultimately surrenders control over the situation to the application framework
(RCP). In RCP the error message of the exception usually gets displayed to the end user in a GUI
dialog and logged to the frameworks log file. Tomcat usually also logs the error and creates a new
thread for the crashed one. This option should be used very sparingly, unforeseen consequences are
likely to occur.

```java
void setServerPort(String value) {
    try {
        serverPort = Integer.parseInt(value);
    } catch (NumberFormatException e) {
        throw new RuntimeException("port " + value " is invalid, ", e);
    }
}
```

### Logging an Exception

If nothing else, an exception must get logged at the very least.

```java
void setServerPort(String value) {
    try {
        serverPort = Integer.parseInt(value);
    } catch (NumberFormatException e) {
        LOGGER.error("invalid port, use default port instead");
    }
}
```

## Logging

In Verinice the logging framework [_log4j_](http://logging.apache.org/log4j) is used for all logging
tasks.

The following log levels should be used in compliance with their description:

* `ERROR`: An unrecoverable unexpected condition occurred. Potential data loss or corruption
  occurred.
* `WARN`: An unexpected condition that should be documented occurred but operation of Verinice can
  be continued without data loss or corruption.
* `INFO`: Informative statements about the execution of Verinice. It must be taken care to not let
  those statements impede the performance of Verinice's execution. Valid examples include:
  * To inform about successful startup of Verinice.
  * To inform about the successful completion of a complex task (like report generation, GSTOOL
    import and the like).
* `DEBUG`: To be used for isolating problems and provide context information (thread IDs, session
  IDs etc.). As all kinds of log messages, debug messages shall be used with care and designed to
  convey a meaningful message to the reader.

  If the generation of the debug message involves a complex computation, it can be appropriate to
  wrap the call like this: `if (LOGGER.isDebugEnabled()) { [...] }`
