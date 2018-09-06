package com.anwesh.uiprojects.linefoldingupview

/**
 * Created by anweshmishra on 06/09/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.content.Context
import android.graphics.Color

val nodes : Int = 5

fun Canvas.drawLFUNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.strokeWidth = Math.min(w, h) / 60
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = Color.parseColor("#0D47A1")
    val gap : Float = (w) / (nodes + 1)
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(0f, scale - 0.5f)) * 2
    save()
    translate(gap/3 + gap * i + gap/2, 0.9f * h - sc2 * (0.9f * h - gap/2))
    for (j in 0..1) {
        save()
        rotate(90f * -sc1 * (1 - 2 * j))
        drawLine(0f, 0f, gap/2 * (1 - 2 * j), 0f, paint)
        restore()
    }
    restore()
}

class LineFoldingUpView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {
        fun update(cb : (Float) -> Unit) {
            scale += 0.1f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {
        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class LFUNode(var i : Int, val state : State = State()) {
        var prev : LFUNode? = null
        var next : LFUNode? = null
        fun addNeighbor() {
            if (i < nodes - 1) {
                next = LFUNode(i + 1)
                next?.prev = this
            }
        }

        init {
            addNeighbor()
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawLFUNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : LFUNode {
            var curr : LFUNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LineFoldingUp(var i : Int) {
        private val root : LFUNode = LFUNode(0)
        private var curr : LFUNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }
    }

    data class Renderer(var view : LineFoldingUpView) {

        private val animator : Animator = Animator(view)
        private var lineFoldingUp : LineFoldingUp = LineFoldingUp(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            lineFoldingUp.draw(canvas, paint)
            animator.animate {
                lineFoldingUp.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            lineFoldingUp.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : LineFoldingUpView {
            val view : LineFoldingUpView = LineFoldingUpView(activity)
            activity.setContentView(view)
            return view
        }
    }
}
