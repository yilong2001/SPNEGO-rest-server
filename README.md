# SPNEGO-rest-server
kerberos web server support SPENGO and a client(curl --negotiate )

###特性
*    支持SPNEGO 机制的kerberos认证
*    基于 Spring Boot & Spring Cloud 
*    通过 不同 url 区分支持 http basic auth 和 http negotiate auth
*    提供了client 实例：功能上与 kinit + curl --negotiate -u : http://xxx 相同

###使用
*    支持 SPNEGO 的 rest server
*    支持 kerberos 认证的 rest server
*    支持 basic 认证的 rest server
*    支持 SPNEGO 的 客户端

