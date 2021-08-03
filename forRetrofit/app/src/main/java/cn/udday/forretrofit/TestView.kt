package cn.udday.forretrofit

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

class TestView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr:Int
) : View(context,attrs,defStyleAttr) {
    //这里是把每个构造函数都调用到第三个，统一在第三个里面init我们的自定义属性
    constructor(context: Context) : this(context,null)
    constructor(context: Context,attrs:AttributeSet?):this(context,attrs,0)
    var myColor:Int = Color.BLACK
    val paint = Paint()
    val canvas = Canvas()
    init {
        //这里是得到我们的自定义属性，`R.styleable.TestView`就是`attrs`里面写了的自定义View的名字,其余继承就好,最后一个不用管默认填0
        val mTypeArray:TypedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.TestView, defStyleAttr, 0)
        //这里是通过得到的`mTypeArray`来得到属性,第一个字填自动生成的`R.styleable.TestView_myColor`,第二个值填默认值,有些不需要默认值
        myColor = mTypeArray.getColor(R.styleable.TestView_myColor, context.getColor(R.color.white))
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        println("TTTT"+myColor)
        paint.setColor(myColor)
        paint.style = Paint.Style.FILL
        val rect = Rect(0,0,200,200)
        canvas.drawRect(rect,paint)
    }
}