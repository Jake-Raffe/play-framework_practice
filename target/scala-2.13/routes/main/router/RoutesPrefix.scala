// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/jacob.raffe/Documents/scala-practice/play-framework/play-template/conf/routes
// @DATE:Wed Jun 22 09:58:37 BST 2022


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
