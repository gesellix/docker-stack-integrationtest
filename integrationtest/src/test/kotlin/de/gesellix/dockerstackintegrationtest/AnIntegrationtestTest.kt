package de.gesellix.dockerstackintegrationtest

import com.squareup.moshi.Moshi
import mu.KLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AnIntegrationtest {

    companion object : KLogging()

    private val client = OkHttpClient()

    private fun get(url: String): String? {
        val request = Request.Builder()
                .url(url)
                .get()
                .build()
        val response = client.newCall(request).execute()
        return response.body()?.string()
    }

    private fun awaitCondition(timeout: Long, unit: TimeUnit, body: () -> Boolean): Boolean {
        val awaitCondition = CountDownLatch(1)
        CompletableFuture.runAsync({
            while (true) {
                try {
                    val conditionFulfilled = body()
                    if (conditionFulfilled) {
                        awaitCondition.countDown()
                        break
                    } else {
                        logger.info(".")
                        Thread.sleep(2000)
                    }
                } catch (ignored: Exception) {
                    logger.info(",")
                    Thread.sleep(2000)
                }
            }
        })
        return awaitCondition.await(timeout, unit)
    }

    @Test
    fun couchdbIsUp() {
        val success = awaitCondition(10, TimeUnit.SECONDS, {
            val response = get("http://localhost:5984/_up")
            logger.info("couchdb/_up response: $response")
            response?.trim() == "{\"status\":\"ok\"}"
        })
        Assert.assertTrue("couchdb is not up.", success)
    }

    @Test
    fun elasticsearchIsUp() {
        val moshi = Moshi.Builder().build()
        val esClusterHealthJsonAdapter = moshi.adapter(ElasticsearchClusterHealth::class.java)

        val success = awaitCondition(20, TimeUnit.SECONDS, {
            val response = get("http://localhost:9200/_cluster/health")
            logger.info("elastic/health response: $response")
            if (response != null) {
                val esClusterStatus = esClusterHealthJsonAdapter.fromJson(response)?.status
                listOf("green", "yellow").contains(esClusterStatus)
            } else {
                false
            }
        })
        Assert.assertTrue("ElasticSearch is not up.", success)
    }
}
