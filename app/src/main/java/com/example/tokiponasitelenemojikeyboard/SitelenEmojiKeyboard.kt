package com.example.tokiponasitelenemojikeyboard

import android.annotation.SuppressLint
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.example.tokiponasitelenemojikeyboard.R.*
import kotlinx.android.synthetic.main.keyboard.view.*


class CapsPress : View.OnClickListener {
    private var caps = false
    private var alphabets : Array<TextView>  = emptyArray()
    constructor(a : Array<TextView>) {
        caps = false
        alphabets = a
    }

    fun reset () {
        if (caps) {
            for (b in alphabets) {
                b.text = (b.getText() as String).toLowerCase()
            }
        }
        caps = false
    }
    override fun onClick(v: View?) {
        if (caps) reset()
        else {
            caps = true
            for (b in alphabets) { b.text = (b.getText() as String).toUpperCase() }
        }
    }

}
class SitelenEmojiKeyboard : InputMethodService() {
//    var suggestions :  Array<TextView> = TODO()
    private lateinit var capsobj : CapsPress
    private lateinit var view: View
    private lateinit  var alphabets : Array<TextView>
    private lateinit var suggestions : Array<TextView>
    private var buffer = ""
    private var capsmode = false

    val alphs = arrayOf('a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z')
    var upperalphs : ArrayList<Char> = ArrayList()
    val horizontals : Array<Array<Char>> = arrayOf(arrayOf('s'),arrayOf('n'),emptyArray(),arrayOf('s'),arrayOf('w'),
        emptyArray(),emptyArray(),arrayOf('j'),arrayOf('u','o'),arrayOf('k'),arrayOf('l','j'),arrayOf('k'),
        arrayOf('n'),arrayOf('m'),arrayOf('p','i'),arrayOf('o'),arrayOf('w'),arrayOf('e','t'),arrayOf('a'),emptyArray(),
        arrayOf('i'),emptyArray(),arrayOf('e'),emptyArray(),arrayOf('u','t'),emptyArray(),emptyArray())
    val allalphs = arrayOf('a','e','i','o','u','n','w','t','p','s','j','k','l','m')
    val vowels = arrayOf('a','e','i','o','u')
    val consonants = arrayOf('w','t','p','s','j','k','l','m')

    lateinit var words : Array<String>
    lateinit var emojis : Array<String>
    lateinit var places : Array<String>
    lateinit var placemojis : Array<String>

    private fun addText (text:CharSequence) {
        val input = currentInputConnection
        input.commitText(text, text.length)
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.playSoundEffect(AudioManager.FX_KEY_CLICK)
    }

    override fun onCreateInputView(): View {

        view = layoutInflater.inflate(layout.keyboard, null)

        for (a in alphs) {
            upperalphs.add(a.toUpperCase())
        }
        alphabets = arrayOf(view.keyA,view.keyB,view.keyC,view.keyD,view.keyE,view.keyF,view.keyG,
            view.keyH, view.keyI, view.keyJ, view.keyK, view.keyL, view.keyM, view.keyN, view.keyO,
            view.keyP, view.keyQ, view.keyR, view.keyS, view.keyT, view.keyU, view.keyV, view.keyW,
            view.keyX, view.keyY, view.keyZ)

        this.capsobj = CapsPress(alphabets)
        suggestions = arrayOf(view.suggestion1,view.suggestion2,view.suggestion3)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val choice = prefs.getString("emojiset", "default")

        if (choice == "default") {
            words = resources.getStringArray(array.default_words)
            emojis = resources.getStringArray(array.default_emojis)
        } else if (choice == "pu-only") {
            words = resources.getStringArray(array.pu_words)
            emojis = resources.getStringArray(array.pu_emojis)
        } else { // Nimi ale pona
            words = resources.getStringArray(array.default_words) + resources.getStringArray(array.nap_words)
            emojis = resources.getStringArray(array.default_emojis) + resources.getStringArray(array.nap_emojis)
        }

        places = resources.getStringArray(array.place_names)
        placemojis = resources.getStringArray(array.place_emojis)

        for (b in alphabets) {
            b.setOnClickListener {
                addText(b.text)
            }
        }

        view.keyCaps.setOnClickListener(capsobj)
        view.keyDelete.setOnClickListener{
            val input = currentInputConnection
            input.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
            input.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
        }
        view.keySpace.setOnClickListener{
            if (!collapseBuffer()) { addText(" ")}
        }
        view.keyColon.setOnClickListener{
            collapseBuffer()
            addText("➗️")
        }
        view.keyName.setOnClickListener{
            collapseBuffer()
            addText("\uD83D\uDD23")
        }

        view.keyPeriod.setOnClickListener{
            collapseBuffer()
            addText("➖️")
        }
        view.keyNextLine.setOnClickListener{
            collapseBuffer()
            val input = currentInputConnection
            input.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER))
            input.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_ENTER))
        }
        view.keyInputSettings.setOnClickListener{
            collapseBuffer()
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }

        view.suggestion1.setOnClickListener{collapseBuffer(0,true)}
        view.suggestion2.setOnClickListener{collapseBuffer(1,true)}
        view.suggestion3.setOnClickListener{collapseBuffer(2,true)}

        val buttons = alphabets + view.keyCaps + view.keyDelete + view.keySpace +
                view.keyColon + view.keyName + view.keyPeriod + view.keyNextLine + view.keyInputSettings
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val darkmode = sharedPref.getBoolean("darkmode",false)
        if (darkmode) {
            view.suggestionlayout.setBackgroundColor(resources.getColor(color.primarySuggestionsDark))
            view.GridKeyboard.setBackgroundColor(resources.getColor(color.primaryBackgroundDark))
            for (b in buttons) {
                b.setTextColor(resources.getColor(color.primaryTextDark))
                b.setBackgroundResource(drawable.buttondark)
            }
        }

        return view
    }

    @SuppressLint("ResourceAsColor")
    override fun onUpdateSelection(oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        updateBuffer()
    }


    private fun updateBuffer () : List<Int> {
        this.capsobj.reset()
        buffer = ""
        capsmode = false
        val input = currentInputConnection
        val before = input.getTextBeforeCursor(100,0)
        if (before != null) {
            for (i in 1..before.length) {
                val a = before[before.length - i]
                if (a in alphs) {
                    buffer = a.toString() + buffer
                } else if (a in upperalphs) {
                    buffer = a.toString() + buffer
                    capsmode = true
                    break
                } else {
                    break
                }
            }
        }
        val beforelen = buffer.length

        val after = input.getTextAfterCursor(100,0)
        if (after != null) {
            for (a in after) {
                if (a in alphs) {
                    buffer += a
                } else {
                    break
                }
            }
        }
        updateSuggestions()
        return listOf(beforelen,buffer.length-beforelen)
    }

    private fun possibleWords (s:String, prev: Char) : ArrayList<String> {
        var string = s // so its mutable
        if (string == "") {
            return arrayListOf("")
        }
        if (string.length > 7) {
            string = string.substring(0, 7)
        }
        var letters = ArrayList<String>() // Possible letters this one could represent
        if (string[0] in allalphs) {
            letters.add(string[0].toString())
        }
        var i = alphs.indexOf(string[0])
        if (i == -1) { i = 26 } // hardcoded null array
        for (char in horizontals[i]) {
            // n is in neither consonants nor vowels
            if (char in vowels && prev !in vowels || char !in vowels && prev !in consonants) {
                letters.add(char.toString())
            }
        }
        letters.add("") // Current letter might just be a flat out mistake, but thats the lowest priority
        var strings = ArrayList<String>()
        for (letter in letters) {
            val after = string.substring(1)
            var nextprev = 'n'
            if (letter.length > 0) { nextprev = letter[0] }
            for (word in possibleWords(after,nextprev)) {
                strings.add(letter + word)
            }
        }
        return strings
    }

    private fun updateSuggestions() {
        this.capsobj.reset()
        val b = buffer.toLowerCase()

        for (i in suggestions) {
            i.text = ""
        }
        if (b == "") {return}

        val possibles = possibleWords(b, 'n')
        var allwords = words
        var allemojis = emojis

        if (capsmode) {
            allwords = places
            allemojis = placemojis
        }
        var suggestionwords = ArrayList<String>()

        for (possible in possibles) {
            val i = allwords.indexOf(possible)
            if (i != -1 && possible !in suggestionwords) {
                suggestions[suggestionwords.size].text = allemojis[i]
                suggestionwords.add(possible)
                if (suggestionwords.size >= 3) {break}
            }

            for (j in 0..(allwords.size-1)) {
                val word = allwords[j]

                if (word.length > possible.length && word.substring(0,possible.length) == possible && word !in suggestionwords) {
                    suggestions[suggestionwords.size].text = allemojis[j]
                    suggestionwords.add(word)
                    if (suggestionwords.size >= 3) {break}
                }
            }
            if (suggestionwords.size >= 3) {break}
        }
    }

    private fun collapseBuffer(i:Int = 0, force: Boolean = false) : Boolean {
        this.capsobj.reset()
        val input = currentInputConnection
        val replace = suggestions[i].text
        val beforeafter = updateBuffer()

        if ( (force || !capsmode) && replace != "") {
            input.deleteSurroundingText(beforeafter[0],beforeafter[1])
            input.commitText(replace,0)
            return true
        }
        return false
    }
}