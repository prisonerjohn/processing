#!/bin/sh


### -- SETUP WORK DIR -------------------------------------------

if test -d work
then
else
  echo Setting up directories to build P5 under Mac OS X
  mkdir work
#  unzip -q -d work jre.zip
  cp -r ../shared/lib work/
  rm -rf work/lib/CVS
  mkdir work/lib/export
  mkdir work/lib/build
  cp -r ../shared/sketchbook work/
  mkdir work/classes
  # this will copy cvs files intact, meaning that changes
  # could be made and checked back in.. interesting
#  cp dist/run.bat work/
  echo
fi


### -- START BUILDING -------------------------------------------

# move to 'app' directory
cd ../..


### -- BUILD BAGEL ----------------------------------------------
cd ..
# make sure bagel exists, if not, check it out of cvs
if test -d bagel
then 
else
  echo Doing CVS checkout of bagel...
  cvs co bagel
  cd bagel
  cvs update -P
  cd ..
fi
cd bagel

MACOSX_CLASSPATH=/System/Library/Frameworks/JavaVM.framework/Classes/classes.jar:/System/Library/Frameworks/JavaVM.framework/Classes/ui.jar:/System/Library/Frameworks/JavaVM.framework/Home/lib/ext/comm.jar

CLASSPATH=$MACOSX_CLASSPATH

### --- make version with serial for the application
echo Building bagel with serial support
perl make.pl SERIAL
cp classes/*.class ../app/build/macosx/work/classes/

### --- make version without serial for applet exporting
echo Building bagel for export
perl make.pl
cp classes/*.class ../app/build/macosx/work/lib/export/

cd ..
cd app


### -- BUILD PDE ------------------------------------------------

echo Building PDE for JDK 1.3

CLASSPATH=build/macosx/work/classes:build/macosx/work/lib/kjc.jar:build/macosx/work/lib/oro.jar:$MACOSX_CLASSPATH

perl buzz.pl "jikes +D -classpath $CLASSPATH -d build/macosx/work/classes" -dJDK13 *.java

cd build/macosx/work/classes
rm -f ../lib/pde.jar
zip -0q ../lib/pde.jar *.class
cd ../..

