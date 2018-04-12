Cutscene Replacer
=================

Replaces FFXIV cutscenes, allowing the skipping of cutscenes that are
normally not skippable (among other things).

**Note!** This is all experimental, quick-and-dirty stuff that will
probably make your computer explode, poison your cat, curse your offspring
for seven generations and make Zalgo haunt you for the rest of your days.

Use at your own risk and don't blame me if something bad happens. Oh,
and using a third-party program like this is obviously against the FFXIV
User Agreement...


Usage
-----
Run the precompiled .jar in a directory with `0a0000.win32.dat0` and
`0a0000.win32.index` files. That's all. If everything worked, most of
the cutscenes in the base game should now be replaced by another one.

Some further tips to avoid breaking the game:

 * backup the `0a0000.win32.dat0` file before running the program
 * the required data files are in `game/sqpack/ffxiv` directory in the
   game install directory
 * don't run the program from the game install directory, but copy the
   required `0a0000.win32.dat0` and `0a0000.win32.index` files to a
   directory where you have the .jar file
 * after running the program, copy the changed `0a0000.win32.dat0`
   file to the game install directory
 * the program will write a couple of temporary files, which can be
   removed after the program has run

If something goes wrong, you should be able to just replace the
`0a0000.win32.index` file from the backup and everything should still
work.


License
-------
The code is released in public domain, or under
[Creative Commons Zero](https://creativecommons.org/publicdomain/zero/1.0/)
if you need an actual license for legal purposes.
