package com.lytics.android.demo.data

import com.lytics.android.demo.R

object EventRepository {
    private val events: List<Event> = listOf(
        Event(
            1,
            "Alicia Keys",
            "March 17th @ 8 P.M.",
            "Austin, TX",
            "Alicia Keys is an American singer, songwriter, and pianist. A classically trained pianist, " +
                    "Keys started composing songs when she was 12 and was signed at 15 years old by Columbia " +
                    "Records.",
            R.drawable.alicia_keys,
            R.drawable.alicia_keys_thumbnail,
        ),
        Event(
            2,
            "Post Malone",
            "March 19th @ 9 P.M.",
            "Oklahoma City, OK",
            "Post Malone is an American rapper, singer, songwriter, and record producer. Known for his " +
                    "variegated vocals, Malone has gained acclaim for blending genres and subgenres of hip hop, " +
                    "pop, R&B, and trap. His stage name was derived from inputting his birth name into a rap name " +
                    "generator.",
            R.drawable.post_malone,
            R.drawable.post_malone_thumbnail,
        ),
        Event(
            3,
            "Doja Cat",
            "April 1st @ 7:30 P.M.",
            "Houston, TX",
            "Doja Cat is an American singer and rapper. Born and raised in Los Angeles, California, she " +
                    "began making and releasing music on SoundCloud as a teenager. Her song \"So High\" caught " +
                    "the attention of Kemosabe and RCA Records, with which she signed a joint record deal at the " +
                    "age of 17, subsequently releasing her debut EP Purrr! in 2014.",
            R.drawable.doja_cat,
            R.drawable.doja_cat_thumbnail,
        ),
        Event(
            4,
            "Maroon 5",
            "March 18th @ 8 P.M.",
            "Dallas, TX",
            "Maroon 5 is an American pop rock band from Los Angeles, California. It currently " +
                    "consists of lead vocalist Adam Levine, keyboardist and rhythm guitarist Jesse Carmichael, " +
                    "lead guitarist James Valentine, drummer Matt Flynn, keyboardist PJ Morton and multi-" +
                    "instrumentalist and bassist Sam Farrar. Original members Levine, Carmichael, bassist " +
                    "Mickey Madden, and drummer Ryan Dusick first came together as Kara's Flowers in 1994, while " +
                    "they were still in high school.",
            R.drawable.maroon_5,
            R.drawable.maroon_5_thumbnail,
        )
    )

    private val eventIdMap = events.associateBy { it.id }

    fun getEvent(id: Long): Event? {
        return eventIdMap.get(id)
    }

    fun getClosestEvent(): Event {
        return events.last()
    }

    fun getPopularEvents(): List<Event> {
        return events.subList(0, 3)
    }


}