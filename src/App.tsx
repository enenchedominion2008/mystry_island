import {useState} from 'react';
import {playSoundCue} from './audio/soundEffects';
import {AdventureView} from './components/AdventureView';
import {EndingScreen} from './components/EndingScreen';
import {TitleScreen} from './components/TitleScreen';
import {
  dismissSafeModal,
  newTitleState,
  openSafeModal,
  processCommand,
  startNewGame,
  submitSafeCombination,
  toggleSound,
} from './game/engine';
import {GameState} from './game/types';

export default function App() {
  const [state, setState] = useState<GameState>(() => newTitleState(true));

  const updateWithAudio = (updater: (prev: GameState) => GameState) => {
    setState((prev) => {
      const next = updater(prev);
      if (next.lastSoundCue) {
        playSoundCue(next.lastSoundCue, next.soundEnabled);
      }
      return next;
    });
  };

  if (state.phase === 'TITLE') {
    return (
      <TitleScreen
        onStartGame={() => setState((prev) => startNewGame(prev.soundEnabled))}
      />
    );
  }

  if (state.phase === 'VICTORY' || state.phase === 'GAME_OVER') {
    return (
      <EndingScreen
        state={state}
        onPlayAgain={() => setState((prev) => startNewGame(prev.soundEnabled))}
      />
    );
  }

  return (
    <AdventureView
      state={state}
      onSubmitCommand={(cmd) => updateWithAudio((prev) => processCommand(prev, cmd))}
      onOpenSafeDialog={() => setState((prev) => openSafeModal(prev))}
      onDismissSafeDialog={() => setState((prev) => dismissSafeModal(prev))}
      onSubmitSafeCode={(code) => updateWithAudio((prev) => submitSafeCombination(prev, code))}
      onToggleSound={() => setState((prev) => toggleSound(prev))}
      onRestart={() => setState((prev) => startNewGame(prev.soundEnabled))}
    />
  );
}
