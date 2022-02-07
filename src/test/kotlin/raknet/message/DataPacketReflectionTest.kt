package raknet.message

import benchmark_kt.Benchmark
import benchmark_kt.BenchmarkTarget
import benchmark_kt.BenchmarkType
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
                packet.encode()
            },
            BenchmarkTarget("TestPacketWithAuto") {
                val value = Random.nextInt()
                val packet = TestMessageWithAuto(
                    value.toLong(),
                    "Hello World",
                    value
                )
                packet.encode()
            }
        ),
        type = BenchmarkType.Operations(1_000_000)
    )
    benchmark.start()
}