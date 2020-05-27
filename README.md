# decloj

A minimal clojure + Qt5 project that compiles with GraalVM to a native image.

## Installation

Clone the repo.

build:

    $ make clean all GRAALVM_HOME=/path/to/graal

results in:

    $ file build/decloj
    build/decloj: ELF 64-bit LSB executable, x86-64, version 1 (SYSV), dynamically linked, interpreter /lib64/ld-linux-x86-64.so.2, for GNU/Linux 2.6.32, BuildID[sha1]=2eb8eb4a562678cc3df78c92276f92e9f8889b03, not stripped
    $ du -sh build/decloj
    62M	build/decloj
    $ ldd build/decloj
    linux-vdso.so.1 =>  (0x00007ffeabdfe000)
    libstdc++.so.6 => /usr/lib/x86_64-linux-gnu/libstdc++.so.6 (0x00007fe1add7b000)
    libpthread.so.0 => /lib/x86_64-linux-gnu/libpthread.so.0 (0x00007fe1adb5e000)
    libdl.so.2 => /lib/x86_64-linux-gnu/libdl.so.2 (0x00007fe1ad95a000)
    libz.so.1 => /lib/x86_64-linux-gnu/libz.so.1 (0x00007fe1ad740000)
    librt.so.1 => /lib/x86_64-linux-gnu/librt.so.1 (0x00007fe1ad538000)
    libc.so.6 => /lib/x86_64-linux-gnu/libc.so.6 (0x00007fe1ad16e000)
    libm.so.6 => /lib/x86_64-linux-gnu/libm.so.6 (0x00007fe1ace65000)
    /lib64/ld-linux-x86-64.so.2 (0x00007fe1ae0fd000)
    libgcc_s.so.1 => /lib/x86_64-linux-gnu/libgcc_s.so.1 (0x00007fe1acc4f000)

Run:

    build/decloj

## Usage

FIXME: explanation

    $ java -jar decloj-0.1.0-standalone.jar [args]

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

 * Only works with linux .so files atm.
 * needs .so files expanded to directory

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2020 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
