/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/*
 * This file is a modified version of the java.util.concurrent.FutureTask
 * class. The original files and thus this file are subject to the GNU GPL
 * Licence version 2 , additionally the following notice accompanied the
 * original version of this file:
 *
 * "
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * "
 */

package microtrafficsim.core.vis.context.tasks;

import microtrafficsim.core.vis.context.RenderContext;

import java.util.concurrent.*;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;


public class FutureRenderTask<V> implements Future<V> {

    private Synchronizer sync;

    public FutureRenderTask(RenderTask<V> task) {
        if (task == null) throw new NullPointerException();
        this.sync = new Synchronizer(task);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return sync.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return sync.isCancelled();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return sync.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return sync.get(timeout, unit);
    }

    /**
     * Sets this Future to the result of its computation unless it has been
     * cancelled.
     *
     * @param context the RenderContext on which this task will be executed
     */
    public void run(RenderContext context) {
        sync.run(context);
    }

    /**
     * Protected method invoked when this task transitions to state isDone
     * (whether normally or via cancellation). The default implementation
     * does nothing. Subclasses may override this method to invoke completion
     * callbacks or perform bookkeeping. Note that you can query status inside
     * the implementation of this method to determine whether this task has
     * been cancelled.
     */
    protected void done() {}

    /**
     * Sets the result of this future to the given value unless this future
     * has already been set or has been cancelled.
     *
     * This method is invoked internally by the <tt>run()</tt> method upon
     * successful completion of the computation.
     *
     * @param value         the value
     */
    protected void set(V value) {
        sync.set(value);
    }

    /**
     * Causes this future to report an ExecutionException with the given
     * throwable as its cause, unless this future has already been set or
     * has been cancelled.
     *
     * This method is invoked internally by the <tt>run()</tt> method upon
     * successful completion of the computation.
     *
     * @param exception     cause of the failure
     */
    protected void setException(Throwable exception) {
        sync.setException(exception);
    }

    /**
     * Executes the computation without setting its result, and then
     * resets this Future to initial state, failing to do so if the
     * computation encounters an exception or is cancelled.  This is
     * designed for use with tasks that intrinsically execute more
     * than once.
     *
     * @param context the RenderContext on which this task will be executed
     * @return true if successfully run and reset
     */
    protected boolean runAndReset(RenderContext context) {
        return sync.runAndReset(context);
    }


    private final class Synchronizer extends AbstractQueuedSynchronizer {

        private static final int READY     = 0;
        private static final int RUNNING   = 1;
        private static final int RAN       = 2;
        private static final int CANCELLED = 4;

        private volatile Thread runner;
        private RenderTask<V> task;
        private V result;
        private Throwable exception;

        Synchronizer(RenderTask<V> task) {
            this.task = task;
            setState(READY);
        }


        boolean isCancelled() {
            return getState() == CANCELLED;
        }

        boolean isDone() {
            return stateIsRanOrCancelled(getState()) && runner == null;
        }

        /**
         * Cancels this task. If the task is already running, the running thread may be
         * interrupted with the {@code mayInterruptIfRunning} flag. If the thread is
         * running and this flag is specified, it depends on the task if the execution
         * finishes normal or is cancelled. In both cases, this method will return true,
         * however if the task still finishes successfully the {@link #isCancelled()}
         * will return false.
         *
         * @param mayInterruptIfRunning set to {@code true} if the running thread should
         *                              be interrupted.
         * @return {@code true} iff the task has not been executed or is currently executing.
         */
        boolean cancel(boolean mayInterruptIfRunning) {
            int state;
            while (true) {
                state = getState();
                if (stateIsRanOrCancelled(state))
                    return false;
                if (state == RUNNING)               // if running, try cancelling
                    break;
                if (compareAndSetState(state, CANCELLED))
                    break;
            }

            if (mayInterruptIfRunning) {
                if (runner != null)
                    runner.interrupt();
            }

            releaseShared(0);

            if (state != RUNNING)
                done();

            return true;
        }

        V get() throws InterruptedException, ExecutionException {
            acquireSharedInterruptibly(0);

            if (getState() == CANCELLED)
                throw new CancellationException();
            if (exception != null)
                throw new ExecutionException(exception);

            return result;
        }

        V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            if (!tryAcquireSharedNanos(0, unit.toNanos(timeout)))
                throw new TimeoutException();
            if (getState() == CANCELLED)
                throw new CancellationException();
            if (exception != null)
                throw new ExecutionException(exception);

            return result;
        }

        void set(V value) {
            while (true) {
                int state = getState();
                if (state == RAN)
                    return;
                if (compareAndSetState(state, RAN)) {
                    result = value;
                    releaseShared(0);
                    done();
                    return;
                }
            }
        }

        void setException(Throwable t) {
            while (true) {
                int state = getState();
                if (state == RAN)
                    return;
                if (state == CANCELLED) {
                    releaseShared(0);
                    return;
                }
                if (compareAndSetState(state, RAN)) {
                    exception = t;
                    releaseShared(0);
                    done();
                    return;
                }
            }
        }

        void run(RenderContext context) {
            if (!compareAndSetState(READY, RUNNING))
                return;

            runner = Thread.currentThread();
            if (getState() == RUNNING) {    // recheck after setting thread
                V result;
                try {
                    result = task.execute(context);
                } catch (CancellationException ex) {
                    setState(CANCELLED);
                    releaseShared(0);
                    return;
                } catch (Throwable ex) {
                    setException(ex);
                    return;
                }
                set(result);
            } else {
                releaseShared(0);           // cancel
            }
        }

        boolean runAndReset(RenderContext context) {
            if (!compareAndSetState(READY, RUNNING))
                return false;

            try {
                runner = Thread.currentThread();
                if (getState() == RUNNING)
                    task.execute(context);
                runner = null;
                return compareAndSetState(RUNNING, READY);
            } catch (Throwable ex) {
                setException(ex);
                return false;
            }
        }


        private boolean stateIsRanOrCancelled(int state) {
            return (state & (RAN | CANCELLED)) != 0;
        }

        @Override
        protected int tryAcquireShared(int unused) {
            return isDone() ? 1 : -1;
        }

        @Override
        protected boolean tryReleaseShared(int unused) {
            runner = null;
            return true;
        }
    }
}
