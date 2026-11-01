import { SessionEngine } from '../SessionEngine';
import { FocusSession } from '../types';

describe('SessionEngine', () => {
  let mockRepo: any;
  let activeSession: FocusSession | null = null;

  beforeEach(() => {
    activeSession = null;
    mockRepo = {
      getActiveSession: jest.fn(async () => activeSession),
      createSession: jest.fn(async (s: FocusSession) => {
        activeSession = s;
      }),
      completeSession: jest.fn(async (id: string) => {
        if (activeSession && activeSession.id === id) {
          activeSession.status = 'COMPLETED';
        }
      }),
      cancelSession: jest.fn(async (id: string) => {
        if (activeSession && activeSession.id === id) {
          activeSession.status = 'CANCELLED';
        }
      }),
    };
  });

  test('starts a quick session when no active session exists', async () => {
    const engine = new SessionEngine(mockRepo);
    const now = 1700000000000;
    const duration = 25 * 60 * 1000; // 25 min

    const res = await engine.startQuickSession(duration, now);
    expect(res.success).toBe(true);
    expect(res.session?.status).toBe('ACTIVE');
    expect(res.session?.startedAtEpochMs).toBe(now);
    expect(res.session?.endsAtEpochMs).toBe(now + duration);
  });

  test('prevents starting a quick session if an unexpired session is already running', async () => {
    const engine = new SessionEngine(mockRepo);
    const now = 1700000000000;
    const duration = 25 * 60 * 1000;

    await engine.startQuickSession(duration, now);

    // Try starting another session 5 minutes later
    const secondTry = await engine.startQuickSession(duration, now + 5 * 60 * 1000);
    expect(secondTry.success).toBe(false);
    expect(secondTry.error).toContain('already in progress');
  });

  test('reconciles expired session to completed', async () => {
    const engine = new SessionEngine(mockRepo);
    const now = 1700000000000;
    const duration = 25 * 60 * 1000;

    await engine.startQuickSession(duration, now);

    // Reconcile 30 minutes later (past end)
    const result = await engine.reconcileState(now + 30 * 60 * 1000);
    expect(result.type).toBe('COMPLETED_EXPIRED');
    expect(activeSession?.status).toBe('COMPLETED');
  });

  test('calculates remaining ms correctly', () => {
    const engine = new SessionEngine(mockRepo);
    const session: FocusSession = {
      id: 'test',
      type: 'QUICK',
      status: 'ACTIVE',
      startedAtEpochMs: 1000,
      endsAtEpochMs: 60000,
      durationMs: 59000,
    };

    expect(engine.calculateRemainingMs(session, 10000)).toBe(50000);
    expect(engine.calculateRemainingMs(session, 70000)).toBe(0);
  });
});
