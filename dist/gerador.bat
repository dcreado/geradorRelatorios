@echo off

set DIRNAME=C:\Documents and Settings\domingos.junior\My Documents\geradorrelatorios

set RUNJAR=%DIRNAME%\geradorRelatorios.jar

set CP=%DIRNAME%\res\
set CP=%CP%;%DIRNAME%\ojdbc14.jar

set JAVA=%JAVA_HOME%\bin\java

set JAVA_OPTS=-Xms256m -Xmx1024m

"%JAVA%" %JAVA_OPTS% -cp "%CP%;%RUNJAR%" br.com.cpqd.gees.relatorios.Main