package kr.ac.kumoh.ce.s20211391.s23w04carddealer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kr.ac.kumoh.ce.s20211391.s23w04carddealer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var main: ActivityMainBinding
    private lateinit var model: CardDealerViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        main = ActivityMainBinding.inflate(layoutInflater)
        setContentView(main.root)

        model = ViewModelProvider(this)[CardDealerViewModel::class.java]
        model.cards.observe(this) { // 값이 변경되면 이미지 가져오기
            val res = IntArray(5)
            for (i in res.indices) {
                res[i] = resources.getIdentifier(
                    getCardName(it[i]),
                    "drawable",
                    packageName
                )
            }
            main.card1.setImageResource(res[0])
            main.card2.setImageResource(res[1])
            main.card3.setImageResource(res[2])
            main.card4.setImageResource(res[3])
            main.card5.setImageResource(res[4])

            val result = getResult(it) // 족보 판별
            main.result.text = result
        }
        main.btnShuffle.setOnClickListener { model.shuffle() }
        simulate()
    }

    private fun getCardName(c: Int) : String{ // 이미지 이름 가져오기
        var shape = when(c / 13){
            0 -> "spades"
            1 -> "diamonds"
            2 -> "hearts"
            3 -> "clubs"
            else -> "error"
        }

        val number = when(c % 13){
            -1 -> "joker"
            0 -> "ace"
            in 1..9 -> (c % 13 + 1).toString()
            10 -> "jack"
            11 -> "queen"
            12 -> "king"
            else -> "error"
        }
        return if(number in arrayOf("joker"))
            "c_red_joker"
        else if(number in arrayOf("jack", "queen", "king"))
            "c_${number}_of_${shape}2"
        else
            "c_${number}_of_${shape}"
    }

    private fun isSameSuit(cards: IntArray): Boolean { // 같은 문양인지 확인
        val firstCardSuit = cards[0] / 13
        return cards.all { it / 13 == firstCardSuit }
    }

    private fun isContinue(cards: IntArray) : Boolean{ // 연속되는지 확인
        for (i in 1 until cards.size) {
            if (cards[i] % 13 - cards[i - 1] % 13 != 1) {
                return false
            }
        }
        return true
    }

    private fun getResult(arr: IntArray): String { // 족보 판별

        if (-1 in arr) { // 맨 처음(조커)인 경우
            return "Click The Button"
        }

        val numCount = mutableMapOf<Int, Int>() // 숫자 카운트

        for (card in arr) {
            val num = card % 13

            numCount[num] = numCount.getOrDefault(num, 0) + 1
        }

        val maxnumCount = numCount.values.maxOrNull() ?: 0
        val continuous = isContinue(arr)
        val sameSuit = isSameSuit(arr)
        val requiredNums1 = setOf(0,9,10,11,12)
        val requiredNums2 = setOf(0,1,2,3,4)
        val exception = numCount.keys.containsAll(requiredNums1)
        val zeroToFour = numCount.keys.containsAll(requiredNums2)

        return when {
            sameSuit && exception -> "로얄 스트레이트 플러쉬"
            sameSuit && zeroToFour -> "백스트레이트 플러쉬"
            sameSuit && (continuous || exception) -> "스트레이트 플러쉬"
            maxnumCount == 4 -> "포커"
            maxnumCount == 3 && numCount.size == 2 -> "풀 하우스"
            sameSuit -> "플러쉬"
            exception -> "마운틴"
            zeroToFour -> "백스트레이트"
            continuous -> "스트레이트"
            maxnumCount == 3 -> "트리플"
            maxnumCount == 2 && numCount.size == 3 -> "투 페어"
            maxnumCount == 2 && numCount.size == 4 -> "원 페어"
            else -> "노 페어"
        }
    }

    fun simulate() {
        val totalSimulations = 100000
        val results = mutableMapOf<String, Int>()
        val deckSize = 52
        val cardsPerHand = 5

        repeat(totalSimulations) {
            val drawnCards = mutableSetOf<Int>()

            while (drawnCards.size < cardsPerHand) {
                val randomCard = (0 until deckSize).random()
                drawnCards.add(randomCard)
            }

            val result = getResult(drawnCards.toIntArray())

            results[result] = results.getOrDefault(result, 0) + 1
        }

        for ((result, count) in results) {
            val probability = count.toDouble() / totalSimulations * 100
            println("시뮬레이션 $result: $count times ($probability%)")
        }
    }
}




