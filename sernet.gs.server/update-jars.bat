 set webinflib=WebContent\WEB-INF\lib
 
call:jar_cmd sernet.gs.ehcache.fragment
call:jar_cmd sernet.hui.common
call:jar_cmd sernet.hui.swtclient
call:jar_cmd sernet.hui.server
call:jar_cmd sernet.gs.service
call:jar_cmd sernet.gs.reveng
call:jar_cmd sernet.verinice.samt.service
call:jar_cmd sernet.verinice.oda.driver
call:jar_cmd sernet.verinice.report.service
call:jar_cmd sernet.verinice.fei.service
goto:eof

:jar_cmd    - here starts my function identified by it's label
jar cf %webinflib%\%~1.jar -C ..\%~1\bin .