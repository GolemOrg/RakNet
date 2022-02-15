package raknet.message

import benchmark_kt.Benchmark
import benchmark_kt.BenchmarkTarget
import benchmark_kt.BenchmarkType
import io.netty.buffer.Unpooled
import kotlin.random.Random

fun main(args: Array<String>) {
    val benchmark = Benchmark(
        name = "DataPacketReflectionTest",
        targets = mutableListOf(
            BenchmarkTarget("TestPacketWithoutAuto") {
                val value = Random.nextInt()
                val packet = TestMessageWithoutAuto(
                    value.toLong(),
                    "Hello World",
                    value
                )
                val buffer = Unpooled.buffer()
                packet.encode(buffer)
            },
            BenchmarkTarget("TestPacketWithAuto") {
                val value = Random.nextInt()
                val packet = TestMessageWithAuto(
                    value.toLong(),
                    "Hello World",
                    value
                )
                val buffer = Unpooled.buffer()
                packet.encode(buffer)
            }
        ),
        type = BenchmarkType.Operations(1_000_000)
    )
    benchmark.start()
}