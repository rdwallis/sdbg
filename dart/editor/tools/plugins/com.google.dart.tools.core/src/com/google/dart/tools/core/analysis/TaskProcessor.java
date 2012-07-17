/*
 * Copyright (c) 2012, the Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.dart.tools.core.analysis;

import com.google.dart.tools.core.DartCore;

/**
 * Executes analysis {@link Task}s that have been placed on a {@link TaskQueue}
 */
public class TaskProcessor {

  private final Object lock = new Object();

  /**
   * The queue from which tasks to be performed are removed (not <code>null</code>)
   */
  private final TaskQueue queue;

  /**
   * The background thread on which analysis tasks are performed or <code>null</code> if the
   * background process has not been started yet.
   */
  private Thread backgroundThread;

  /**
   * <code>true</code> if there are no tasks queued (or analyzing is <code>false</code>) and no
   * tasks being performed. Synchronize against {@link #lock} before accessing this field.
   */
  private boolean isIdle = false;

  /**
   * A collection of objects to be notified when the receiver is idle. Synchronize against
   * {@link #lock} before accessing this field.
   */
  IdleListener[] idleListeners = new IdleListener[0];

  /**
   * Construct a new instance that removes tasks to be performed from the specified queue
   * 
   * @param queue the task queue (not <code>null</code>)
   */
  public TaskProcessor(TaskQueue queue) {
    this.queue = queue;
  }

  /**
   * Add an object to be notified when there are no tasks queued (or analyzing is <code>false</code>
   * ) and no tasks being performed.
   * 
   * @param listener the object to be notified
   */
  public void addIdleListener(IdleListener listener) {
    if (listener == null) {
      return;
    }
    synchronized (lock) {
      for (int i = 0; i < idleListeners.length; i++) {
        if (idleListeners[i] == listener) {
          return;
        }
      }
      int oldLen = idleListeners.length;
      IdleListener[] newListeners = new IdleListener[oldLen + 1];
      System.arraycopy(idleListeners, 0, newListeners, 0, oldLen);
      newListeners[oldLen] = listener;
      idleListeners = newListeners;
    }
  }

  /**
   * Answer <code>true</code> if there are no tasks queued (or analyzing is false) and no tasks
   * being performed. The idle state may change the moment this method returns, so clients should
   * not depend upon this result.
   */
  public boolean isIdle() {
    synchronized (lock) {
      return isIdle;
    }
  }

  /**
   * Remove the specified object from the list of objects to be notified
   * 
   * @param listener the object to be removed
   */
  public void removeIdleListener(IdleListener listener) {
    synchronized (lock) {
      int oldLen = idleListeners.length;
      for (int i = 0; i < oldLen; i++) {
        if (idleListeners[i] == listener) {
          IdleListener[] newListeners = new IdleListener[oldLen - 1];
          System.arraycopy(idleListeners, 0, newListeners, 0, i);
          System.arraycopy(idleListeners, i + 1, newListeners, i, oldLen - 1 - i);
          return;
        }
      }
    }
  }

  /**
   * Start the background analysis process if it has not already been started. This background
   * thread will process tasks from the associated event queue until the
   * {@link TaskQueue#setAnalyzing(boolean)} method is called with a value of <code>false</code>.
   * 
   * @throws IllegalStateException if background thread has already been started
   */
  public void start() {
    synchronized (lock) {
      if (backgroundThread != null) {
        throw new IllegalStateException();
      }
      backgroundThread = new Thread("Analysis Server") {

        @Override
        public void run() {
          try {
            while (true) {

              // Running :: Execute tasks from the queue
              while (true) {
                Task task = queue.removeNextTask();
                // if no longer analyzing or queue is empty, then switch to idle state
                if (task == null) {
                  break;
                }
                try {
                  task.perform();
                } catch (Throwable e) {
                  DartCore.logError("Analysis Task Exception", e);
                }
              }

              // Notify :: Changing state from Running to Idle
              notifyIdle(true);

              // Idle :: Wait for new tasks on the queue... or analysis to be canceled
              if (!queue.waitForTask()) {
                break;
              }

              // Notify :: Changing state from Idle to Running
              notifyIdle(false);

            }
          } catch (Throwable e) {
            DartCore.logError("Analysis Server Exception", e);
          } finally {
            synchronized (lock) {
              backgroundThread = null;
              // Ensure waiting threads are unblocked
              lock.notifyAll();
            }
          }
        }
      };
      backgroundThread.start();
    }
  }

  /**
   * Wait up to the specified number of milliseconds for the receiver to be idle. If the specified
   * number is less than or equal to zero, then this method returns immediately.
   * 
   * @param milliseconds the maximum number of milliseconds to wait
   * @return <code>true</code> if the receiver is idle, else <code>false</code>
   */
  public boolean waitForIdle(long milliseconds) {
    synchronized (lock) {
      long end = System.currentTimeMillis() + milliseconds;
      while (!isIdle) {
        long delta = end - System.currentTimeMillis();
        if (delta <= 0) {
          return false;
        }
        try {
          lock.wait(delta);
        } catch (InterruptedException e) {
          //$FALL-THROUGH$
        }
      }
      return true;
    }
  }

  /**
   * Set the idle state and notify any listeners if the state has changed
   * 
   * @param idle <code>true</code> if the receiver is now idle, else <code>false</code>
   */
  private void notifyIdle(boolean idle) {
    IdleListener[] listenersToNotify;
    synchronized (lock) {
      if (isIdle == idle) {
        return;
      }
      isIdle = idle;
      // notify any threads waiting for state change notification
      lock.notifyAll();
      listenersToNotify = idleListeners;
    }
    for (IdleListener listener : listenersToNotify) {
      try {
        listener.idle(idle);
      } catch (Throwable e) {
        DartCore.logError("Exception during idle notification", e);
      }
    }
  }
}
