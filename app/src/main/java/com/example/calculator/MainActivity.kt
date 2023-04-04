package com.example.calculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.calculator.databinding.ActivityMainBinding
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sqrt


class MainActivity : AppCompatActivity() {
    private var canAddOperation = false
    private var canAddDecimal = true
    var lastClickTime = 0L
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.allClearOnClick.setOnClickListener{clearOnClick()}
        binding.squareButton?.setOnClickListener { squareOnClick() }
        binding.oneToX?.setOnClickListener{oneToX() }

    }

    private fun clearOnClick() {
        val clickDelay = 200L
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < clickDelay) {
            binding.workingTextView.text = ""
            binding.resultTextView.text = ""
        }
        else{
            if(binding.workingTextView.text.isNotEmpty()) {
                binding.workingTextView.text = binding.workingTextView.text.substring(0, binding.workingTextView.text.length - 1)
            }
            lastClickTime = clickTime
        }
            binding.resultTextView.text = ""
    }

    fun numberOnClick(view: View) {
        if (view is Button) {
            if (view.text == ".") {
                if(canAddDecimal) {
                    binding.workingTextView.append(view.text)
                }
                    canAddDecimal = false
            }
            else{
                binding.workingTextView.append(view.text)
                canAddOperation = true
            }
        }
    }
    fun operatorOnClick(view: View) {
        if (view is Button && canAddOperation) {
                binding.workingTextView.append(view.text)
                canAddOperation = false
                canAddDecimal = true
            }
    }

    fun equalsOnClick(view: View) {
        binding.resultTextView.text = calculateResults()
    }

    private fun calculateResults(): String {
        val digitsOperators = digitOperators()
        if(digitsOperators.isEmpty()) return ""

        val timesDivision = timesDivisionCalculate(digitsOperators)
        if(timesDivision.isEmpty()) return ""

        val result = addSubtractCalculate(timesDivision)
        return result.toString()
    }
    private fun squareOnClick() {
        if (binding.workingTextView.text.isNotEmpty()) {
            val value = binding.workingTextView.text.toString().toFloat()
            val result = value.pow(2)
            binding.resultTextView.text = result.toString()
        }
    }
    private fun oneToX() {
        if(binding.workingTextView.text.isNotEmpty()){
            val value = binding.workingTextView.text.toString().toFloat()
            val result = 1 / value
            binding.resultTextView.text = result.toString()
        }
    }
    private fun addSubtractCalculate(passedList: MutableList<Any>): Float {
        var result = passedList[0] as Float
        for(i in passedList.indices){
            if(passedList[i] is Char && i != passedList.lastIndex)
            {
                val operator = passedList[i]
                val nextDigit = passedList[i + 1] as Float
                if(operator == '+'){
                    result += nextDigit
                }
                if(operator == '-'){
                    result -= nextDigit
                }
            }
        }

        return result
    }

    private fun timesDivisionCalculate(passedList: MutableList<Any>): MutableList<Any>  {
        var list = passedList
        while(list.contains('X') || list.contains('÷') || list.contains('%') ||  list.contains('√')){
            list = calcTimesDiv(list)
        }
        return list
    }

    private fun calcTimesDiv(passedList: MutableList<Any>): MutableList<Any> {
        val newList = mutableListOf<Any>()
        var restartIndex = passedList.size

        for(i in passedList.indices){
            if(passedList[i] is Char && i != passedList.lastIndex && i < restartIndex){
                val operator = passedList[i]
                val prevDigit = passedList[i - 1] as Float
                val nextDigit = passedList[i + 1] as Float
                when(operator)
                {
                    'X' ->{
                        newList.add(prevDigit * nextDigit)
                        newList.add(nextDigit)
                        restartIndex = i + 1
                    }
                    '÷' ->{
                        newList.add(prevDigit / nextDigit)
                        newList.add(nextDigit)
                        restartIndex = i + 1
                    }

                    '%' ->{
                        newList.add(prevDigit % nextDigit)
                        newList.add(nextDigit)
                        restartIndex = i + 1
                    }
                    '√' ->{
                        newList.add(prevDigit * sqrt(nextDigit))
                        newList.add(nextDigit)
                        restartIndex = i + 1
                    }
                    else -> {
                        newList.add(prevDigit)
                        newList.add(operator)
                    }
                }

            }
            if(i > restartIndex){
                newList.add(passedList[i])
            }
        }
        return newList
    }

    private fun digitOperators(): MutableList<Any>{
        val list = mutableListOf<Any>()
        var currentDigit = ""
        for((index,char) in binding.workingTextView.text.withIndex()){
            if(char.isDigit() || char == '.') {
                currentDigit += char
            }
            else if (char == '-') { // проверка на минус
                if (currentDigit != "") { // если есть предыдущее число
                    list.add(currentDigit.toFloat()) // добавляем его в список
                    currentDigit = "" // очищаем текущую строку
                    list.add(char) // добавляем минус в список как оператор
                }
                else {
                    if (index == 0) { // если минус первый символ в выражении
                        currentDigit += char // добавляем его к текущей строке
                    }
                    else {
                        val prevChar = binding.workingTextView.text[index - 1] // получаем предыдущий символ в выражении
                        if (prevChar.isDigit() || prevChar == '.') { // если он число или точка
                            list.add(currentDigit.toFloat()) // добавляем текущую строку в список как число
                            currentDigit = "" // очищаем текущую строку
                            list.add(char) // добавляем минус в список как оператор
                        }
                        else { // если он оператор
                            currentDigit += char // добавляем минус к текущей строке
                        }
                    }
                }
            }
            else{
                list.add(currentDigit.toFloat())
                currentDigit = ""
                list.add(char)
            }
        }
        if(currentDigit != ""){
            list.add(currentDigit.toFloat())
        }

        return list
    }

    fun setNegativeOrPositiveOnClick(view: View) {
//            if (view is Button) {
//                if (!binding.workingTextView.text.contains(Regex("[-+%X\"]")) && binding.workingTextView.text.isNotEmpty())  {
//                    binding.workingTextView.text = (-binding.workingTextView.text.toString().toInt()).toString()
//                }
                when (view) {
                    is Button -> {
                        binding.workingTextView.apply {
                            if (text.isNotEmpty()) {
                                val number = text.toString().toIntOrNull()
                                if (number != null) {
                                    text = (-number).toString()
                                }
                            }
                        }
                    }
                }
            }
    }