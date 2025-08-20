package com.example.myapp;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.myapp.models.TeamPlayer;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified Player Selection Modal - Handles all player selection scenarios
 * Three modes: Setup, Quarter Change, and Substitution
 * Same UI, different contexts - clean and consistent UX
 */
public class PlayerSelectionModal extends DialogFragment {
    
    // Selection Modes
    public enum SelectionMode {
        SETUP,          // Select starting 5 (0/5 → 5/5)
        QUARTER_CHANGE, // Modify current lineup between quarters
        SUBSTITUTION    // Replace players during game (flexible patterns)
    }
    
    // Visual States for Player Cards
    private static final int STATE_AVAILABLE = 0;    // Grey - can be selected
    private static final int STATE_SELECTED = 1;     // Blue - selected/going in
    private static final int STATE_ON_COURT = 2;     // Green - currently playing (substitution only)
    private static final int STATE_COMING_OUT = 3;   // Red - being substituted out
    
    // Data
    private SelectionMode mode;
    private String teamName;
    private List<TeamPlayer> allPlayers;
    private List<TeamPlayer> currentLineup; // For quarter change and substitution modes
    private List<TeamPlayer> selectedPlayers;
    private List<TeamPlayer> playersComingOut; // For substitution mode
    
    // UI Components
    private TextView tvModalHeader, tvStatusDisplay;
    private LinearLayout llPlayersContainer;
    private Button btnCancel, btnConfirm;
    private List<View> playerCards;
    private List<Integer> playerStates;
    
    // Callback interface
    public interface PlayerSelectionListener {
        void onPlayersSelected(List<TeamPlayer> selectedPlayers, List<TeamPlayer> playersOut);
        void onSelectionCancelled();
    }
    
    private PlayerSelectionListener listener;
    
    public static PlayerSelectionModal newInstance(SelectionMode mode, String teamName, 
                                                   List<TeamPlayer> allPlayers, 
                                                   List<TeamPlayer> currentLineup) {
        PlayerSelectionModal modal = new PlayerSelectionModal();
        Bundle args = new Bundle();
        args.putSerializable("mode", mode);
        args.putString("teamName", teamName);
        args.putSerializable("allPlayers", new ArrayList<>(allPlayers));
        if (currentLineup != null) {
            args.putSerializable("currentLineup", new ArrayList<>(currentLineup));
        }
        modal.setArguments(args);
        return modal;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_unified_player_selection, container, false);
        
        // Get arguments
        Bundle args = getArguments();
        mode = (SelectionMode) args.getSerializable("mode");
        teamName = args.getString("teamName");
        allPlayers = (List<TeamPlayer>) args.getSerializable("allPlayers");
        currentLineup = (List<TeamPlayer>) args.getSerializable("currentLineup");
        
        // Initialize data structures
        selectedPlayers = new ArrayList<>();
        playersComingOut = new ArrayList<>();
        playerCards = new ArrayList<>();
        playerStates = new ArrayList<>();
        
        // Initialize UI components
        initializeViews(view);
        
        // Configure modal based on mode
        configureModalForMode();
        
        // Create player cards
        createPlayerCards();
        
        // Set up event listeners
        setupEventListeners();
        
        // Update displays
        updateStatusDisplay();
        
        return view;
    }
    
    private void initializeViews(View view) {
        tvModalHeader = view.findViewById(R.id.tvModalHeader);
        tvStatusDisplay = view.findViewById(R.id.tvStatusDisplay);
        llPlayersContainer = view.findViewById(R.id.llPlayersContainer);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnConfirm = view.findViewById(R.id.btnConfirm);
    }
    
    private void configureModalForMode() {
        switch (mode) {
            case SETUP:
                tvModalHeader.setText(teamName + " - Select Starting 5");
                btnConfirm.setText("Set Lineup");
                // Pre-select current lineup if provided
                if (currentLineup != null) {
                    selectedPlayers.addAll(currentLineup);
                }
                break;
                
            case QUARTER_CHANGE:
                tvModalHeader.setText(teamName + " - Quarter Lineup");
                btnConfirm.setText("Update Lineup");
                // Pre-select current lineup
                if (currentLineup != null) {
                    selectedPlayers.addAll(currentLineup);
                }
                break;
                
            case SUBSTITUTION:
                tvModalHeader.setText(teamName + " - Substitution");
                btnConfirm.setText("Make Substitution");
                break;
        }
    }
    
    private void createPlayerCards() {
        llPlayersContainer.removeAllViews();
        playerCards.clear();
        playerStates.clear();
        
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        
        // Create rows of 4 players each for narrow screen compatibility
        int playersPerRow = 4;
        LinearLayout currentRow = null;
        
        for (int i = 0; i < allPlayers.size(); i++) {
            TeamPlayer player = allPlayers.get(i);
            
            // Create new row every 4 players
            if (i % playersPerRow == 0) {
                currentRow = new LinearLayout(getActivity());
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
                llPlayersContainer.addView(currentRow);
            }
            
            // Create player card
            View cardView = inflater.inflate(R.layout.item_modal_player_card, null);
            TextView tvNumber = cardView.findViewById(R.id.tvPlayerNumber);
            TextView tvName = cardView.findViewById(R.id.tvPlayerName);
            
            tvNumber.setText(String.valueOf(player.getNumber()));
            tvName.setText(player.getName());
            
            // Determine initial state
            int initialState = getInitialPlayerState(player);
            playerStates.add(initialState);
            
            // Set initial visual state
            updatePlayerCardVisual(cardView, initialState);
            
            // Set click listener
            final int playerIndex = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPlayerCardClicked(playerIndex);
                }
            });
            
            // Add to current row with equal weight for consistent sizing
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, // 0 width with weight = flexible width
                dpToPx(54), // Fixed height (50dp card + 4dp margins)
                1.0f); // Equal weight for all cards in row
            params.setMargins(2, 2, 2, 2); // Small margins
            
            playerCards.add(cardView);
            currentRow.addView(cardView, params);
        }
    }
    
    private int getInitialPlayerState(TeamPlayer player) {
        switch (mode) {
            case SETUP:
                return selectedPlayers.contains(player) ? STATE_SELECTED : STATE_AVAILABLE;
                
            case QUARTER_CHANGE:
                return selectedPlayers.contains(player) ? STATE_SELECTED : STATE_AVAILABLE;
                
            case SUBSTITUTION:
                if (currentLineup != null && currentLineup.contains(player)) {
                    return STATE_ON_COURT;
                }
                return STATE_AVAILABLE;
                
            default:
                return STATE_AVAILABLE;
        }
    }
    
    private void onPlayerCardClicked(int playerIndex) {
        TeamPlayer player = allPlayers.get(playerIndex);
        int currentState = playerStates.get(playerIndex);
        int newState;
        
        switch (mode) {
            case SETUP:
            case QUARTER_CHANGE:
                // Toggle between available and selected
                if (currentState == STATE_SELECTED) {
                    selectedPlayers.remove(player);
                    newState = STATE_AVAILABLE;
                } else if (selectedPlayers.size() < 5) {
                    selectedPlayers.add(player);
                    newState = STATE_SELECTED;
                } else {
                    // Can't select more than 5
                    return;
                }
                break;
                
            case SUBSTITUTION:
                // Handle substitution state transitions
                if (currentState == STATE_ON_COURT) {
                    // On court → Coming out
                    playersComingOut.add(player);
                    newState = STATE_COMING_OUT;
                } else if (currentState == STATE_COMING_OUT) {
                    // Coming out → On court
                    playersComingOut.remove(player);
                    newState = STATE_ON_COURT;
                } else if (currentState == STATE_AVAILABLE) {
                    // Available → Going in
                    selectedPlayers.add(player);
                    newState = STATE_SELECTED;
                } else if (currentState == STATE_SELECTED) {
                    // Going in → Available
                    selectedPlayers.remove(player);
                    newState = STATE_AVAILABLE;
                } else {
                    return;
                }
                break;
                
            default:
                return;
        }
        
        // Update state and visual
        playerStates.set(playerIndex, newState);
        updatePlayerCardVisual(playerCards.get(playerIndex), newState);
        
        // Update displays
        updateStatusDisplay();
        updateConfirmButton();
    }
    
    private void updatePlayerCardVisual(View cardView, int state) {
        int backgroundColor;
        int textColor = Color.WHITE;
        
        switch (state) {
            case STATE_AVAILABLE:
                backgroundColor = Color.parseColor("#BDC3C7"); // Light grey
                textColor = Color.parseColor("#2C3E50");
                break;
            case STATE_SELECTED:
                backgroundColor = Color.parseColor("#3498DB"); // Blue
                break;
            case STATE_ON_COURT:
                backgroundColor = Color.parseColor("#27AE60"); // Green
                break;
            case STATE_COMING_OUT:
                backgroundColor = Color.parseColor("#E74C3C"); // Red
                break;
            default:
                backgroundColor = Color.parseColor("#BDC3C7");
                textColor = Color.parseColor("#2C3E50");
        }
        
        cardView.setBackgroundColor(backgroundColor);
        TextView tvNumber = cardView.findViewById(R.id.tvPlayerNumber);
        TextView tvName = cardView.findViewById(R.id.tvPlayerName);
        tvNumber.setTextColor(textColor);
        tvName.setTextColor(textColor);
    }
    
    private void updateStatusDisplay() {
        String statusText;
        
        switch (mode) {
            case SETUP:
            case QUARTER_CHANGE:
                statusText = selectedPlayers.size() + "/5 selected";
                break;
                
            case SUBSTITUTION:
                if (playersComingOut.size() == 0 && selectedPlayers.size() == 0) {
                    statusText = "Making substitution...";
                } else if (playersComingOut.size() == selectedPlayers.size()) {
                    statusText = selectedPlayers.size() + " player" + 
                               (selectedPlayers.size() == 1 ? "" : "s") + " swapping";
                } else {
                    statusText = "Must have equal players in/out";
                }
                break;
                
            default:
                statusText = "";
        }
        
        tvStatusDisplay.setText(statusText);
    }
    
    private void updateConfirmButton() {
        boolean isValid = false;
        
        switch (mode) {
            case SETUP:
            case QUARTER_CHANGE:
                isValid = selectedPlayers.size() == 5;
                break;
                
            case SUBSTITUTION:
                isValid = playersComingOut.size() > 0 && 
                         playersComingOut.size() == selectedPlayers.size();
                break;
        }
        
        btnConfirm.setEnabled(isValid);
        btnConfirm.setBackgroundColor(isValid ? 
                                     Color.parseColor("#3498DB") : 
                                     Color.parseColor("#BDC3C7"));
    }
    
    private void setupEventListeners() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onSelectionCancelled();
                }
                dismiss();
            }
        });
        
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onPlayersSelected(new ArrayList<>(selectedPlayers), 
                                             new ArrayList<>(playersComingOut));
                }
                dismiss();
            }
        });
    }
    
    public void setPlayerSelectionListener(PlayerSelectionListener listener) {
        this.listener = listener;
    }
    
    // Utility method to convert dp to pixels for consistent sizing
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
