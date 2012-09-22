package collins.graphs

import util.config.Configurable
import collins.cache.ConfigCache

object GraphConfig extends Configurable {
  override val namespace = "graph"
  override val referenceConfigFilename = "graph_reference.conf"

  lazy protected[graphs] val queryCache =
    ConfigCache.create(GraphConfig.cacheTimeout, GraphConfig.cacheSize, MetricsCacheLoader())

  def enabled = getBoolean("enabled", false)
  def className = getString("class", "collins.graphs.FibrGraphs")
  def cacheSize = getInt("queryCacheSize", 2000)
  def cacheTimeout = getMilliseconds("queryCacheTimeout").getOrElse(60000L)

  def getConfigForClass() = {
    val name = className.split('.').last
    getConfig(name)
  }
  override def validateConfig() {
    if (enabled) {
      className
      getConfigForClass
    }
  }
}
