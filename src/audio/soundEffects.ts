import {SoundCue} from '../game/types';

let sharedAudioCtx: AudioContext | null = null;

function getAudioContext(): AudioContext | null {
  if (typeof window === 'undefined') return null;
  const AudioCtx =
    window.AudioContext ||
    (window as unknown as {webkitAudioContext?: typeof AudioContext}).webkitAudioContext;
  if (!AudioCtx) return null;
  if (!sharedAudioCtx) {
    sharedAudioCtx = new AudioCtx();
  }
  if (sharedAudioCtx.state === 'suspended') {
    sharedAudioCtx.resume().catch(() => {});
  }
  return sharedAudioCtx;
}

const CUE_SEQUENCES: Record<SoundCue, Array<[number, number]>> = {
  MOVE: [[220, 0.055]],
  PICKUP: [
    [523.25, 0.07],
    [659.25, 0.09],
  ],
  SOLVE: [
    [440, 0.08],
    [554.37, 0.08],
    [659.25, 0.14],
  ],
  RESTORE: [
    [392, 0.08],
    [523.25, 0.12],
  ],
  WARNING: [
    [196, 0.11],
    [164.81, 0.13],
  ],
  VICTORY: [
    [523.25, 0.1],
    [659.25, 0.1],
    [783.99, 0.12],
    [1046.5, 0.22],
  ],
  DEFEAT: [
    [246.94, 0.14],
    [220, 0.16],
    [174.61, 0.25],
  ],
};

export function playSoundCue(cue: SoundCue, enabled: boolean): void {
  if (!enabled) return;
  try {
    const ctx = getAudioContext();
    if (!ctx) return;

    const notes = CUE_SEQUENCES[cue];
    let offset = ctx.currentTime;

    for (const [freq, duration] of notes) {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(freq, offset);

      gain.gain.setValueAtTime(0.001, offset);
      gain.gain.exponentialRampToValueAtTime(0.08, offset + 0.01);
      gain.gain.exponentialRampToValueAtTime(0.001, offset + duration);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start(offset);
      osc.stop(offset + duration + 0.01);
      offset += duration * 0.9;
    }
  } catch {
    // Best-effort audio feedback.
  }
}
