:: Code Exam2
echo off
echo '............................................'
echo '..     ALGONQUIN COLLEGE - JAP - 25S      ..'
echo '............................................'
echo '                                            '
echo '  ........=====...........................  '
echo '  ......=+===.............................  '
echo '  ......===+=........=+...................  '
echo '  .......===.........=+...=+.=+..+=...+=..  '
echo '  ...=========....==.=+.+=.=.+=+===.+=.=..  '
echo '  ....==========...==+..====...==...====..  '
echo '  ..==.=======............................  '
echo '  ...=============........................  '
echo '.                                           '
echo '.         [Team: Heta / Khusmit]         '
echo '............................................'
set "arg=%1"
if "%arg%"=="" set "arg=qtouch"
echo Parameter value: %arg%
echo 'Starting Javadoc ...........................'
javadoc -d %arg%_doc ./src/%arg%/*.java
echo 'Compiling ..................................'
javac ./src/%arg%/*.java -d bin
echo 'Creating Jar ...............................'
jar cfm %arg%.jar manifest-exam.txt -C bin %arg%
echo 'Running Jar ................................'
java -jar %arg%.jar
echo 'End ........................................'