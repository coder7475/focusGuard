import { FocusSession } from './types';
import { SessionRepository, sessionRepository } from '../data/repositories/SessionRepository';
import FocusEnforcer from '../modules/FocusEnforcer';

export type ReconciliationResult =
  | { type: 'NO_ACTIVE_SESSION' }
  | { type: 'STILL_ACTIVE'; session: FocusSession; remainingMs: number }
  | { type: 'COMPLETED_EXPIRED'; session: FocusSession };

export class SessionEngine {
  constructor(private repo: SessionRepository = sessionRepository) {}

  async startQuickSession(
    durationMs: number,
    nowEpochMs: number = Date.now()
  ): Promise<{ success: boolean; session?: FocusSession; error?: string }> {
    const existingActive = await this.repo.getActiveSession();
    if (existingActive && existingActive.status === 'ACTIVE') {
      if (nowEpochMs < existingActive.endsAtEpochMs) {
        return { success: false, error: 'An active session is already in progress' };
      } else {
        // Auto-complete expired session
        await this.repo.completeSession(existingActive.id);
      }
    }

    const session: FocusSession = {
      id: `session_${nowEpochMs}`,
      type: 'QUICK',
      status: 'ACTIVE',
      startedAtEpochMs: nowEpochMs,
      endsAtEpochMs: nowEpochMs + durationMs,
      durationMs,
    };

    await this.repo.createSession(session);

    // Trigger native foreground lock & alarm
    try {
      FocusEnforcer.startFocusLock(session.endsAtEpochMs);
    } catch {}

    return { success: true, session };
  }

  async completeActiveSession(
    sessionId?: string
  ): Promise<FocusSession | null> {
    const session = sessionId
      ? await this.repo.getActiveSession()
      : await this.repo.getActiveSession();

    if (!session) return null;

    await this.repo.completeSession(session.id);

    try {
      FocusEnforcer.stopFocusLock();
    } catch {}

    return { ...session, status: 'COMPLETED' };
  }

  async cancelActiveSession(): Promise<FocusSession | null> {
    const session = await this.repo.getActiveSession();
    if (!session) return null;

    await this.repo.cancelSession(session.id);

    try {
      FocusEnforcer.stopFocusLock();
    } catch {}

    return { ...session, status: 'CANCELLED' };
  }

  async reconcileState(nowEpochMs: number = Date.now()): Promise<ReconciliationResult> {
    const session = await this.repo.getActiveSession();
    if (!session || session.status !== 'ACTIVE') {
      return { type: 'NO_ACTIVE_SESSION' };
    }

    if (nowEpochMs >= session.endsAtEpochMs) {
      await this.repo.completeSession(session.id);
      try {
        FocusEnforcer.stopFocusLock();
      } catch {}
      return { type: 'COMPLETED_EXPIRED', session: { ...session, status: 'COMPLETED' } };
    }

    const remainingMs = session.endsAtEpochMs - nowEpochMs;
    return { type: 'STILL_ACTIVE', session, remainingMs };
  }

  calculateRemainingMs(session: FocusSession, nowEpochMs: number = Date.now()): number {
    if (session.status !== 'ACTIVE') return 0;
    return Math.max(0, session.endsAtEpochMs - nowEpochMs);
  }
}

export const sessionEngine = new SessionEngine();
