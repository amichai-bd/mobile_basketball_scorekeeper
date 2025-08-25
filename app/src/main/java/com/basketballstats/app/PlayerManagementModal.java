package com.basketballstats.app;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.basketballstats.app.models.Team;
import com.basketballstats.app.models.TeamPlayer;
import com.basketballstats.app.data.DatabaseController;
import java.util.ArrayList;
import java.util.List;

/**
 * Player Management Modal - Complete CRUD operations for team rosters
 * Features: Add players, edit players, delete players, validation, empty state handling
 */
public class PlayerManagementModal {
    
    private Context context;
    private Team team;
    private Dialog dialog;
    private OnPlayersChangedListener listener;
    
    // UI Components
    private TextView tvModalTitle;
    private EditText etJerseyNumber, etPlayerName;
    private Button btnAddPlayer, btnSaveChanges, btnCancel, btnCloseModal;
    private ListView lvPlayers;
    private TextView tvPlayerCount, tvEmptyState, tvValidationMessage;
    
    // Data
    private List<TeamPlayer> playersList;
    private PlayerManagementAdapter playersAdapter;
    private boolean hasUnsavedChanges = false;
    
    // Interface for callback when players are modified
    public interface OnPlayersChangedListener {
        void onPlayersChanged(Team team);
    }
    
    public PlayerManagementModal(Context context, Team team, OnPlayersChangedListener listener) {
        this.context = context;
        this.team = team;
        this.listener = listener;
        this.playersList = new ArrayList<>(team.getPlayers()); // Create copy for editing
    }
    
    /**
     * Show the player management modal
     */
    public void show() {
        // Create full-screen dialog
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_player_management);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        
        initializeViews();
        setupEventListeners();
        setupValidation();
        refreshPlayersList();
        
        dialog.show();
    }
    
    private void initializeViews() {
        // Header
        tvModalTitle = dialog.findViewById(R.id.tvModalTitle);
        btnCloseModal = dialog.findViewById(R.id.btnCloseModal);
        tvModalTitle.setText(team.getName() + " - Player Management");
        
        // Add Player Section
        etJerseyNumber = dialog.findViewById(R.id.etJerseyNumber);
        etPlayerName = dialog.findViewById(R.id.etPlayerName);
        btnAddPlayer = dialog.findViewById(R.id.btnAddPlayer);
        tvValidationMessage = dialog.findViewById(R.id.tvValidationMessage);
        
        // Players List
        lvPlayers = dialog.findViewById(R.id.lvPlayers);
        tvPlayerCount = dialog.findViewById(R.id.tvPlayerCount);
        tvEmptyState = dialog.findViewById(R.id.tvEmptyState);
        
        // Footer
        btnCancel = dialog.findViewById(R.id.btnCancel);
        btnSaveChanges = dialog.findViewById(R.id.btnSaveChanges);
        
        // Setup adapter
        playersAdapter = new PlayerManagementAdapter(context, playersList);
        lvPlayers.setAdapter(playersAdapter);
    }
    
    private void setupEventListeners() {
        // Close modal (discard changes)
        btnCloseModal.setOnClickListener(v -> handleCancel());
        btnCancel.setOnClickListener(v -> handleCancel());
        
        // Save changes
        btnSaveChanges.setOnClickListener(v -> handleSaveChanges());
        
        // Add player
        btnAddPlayer.setOnClickListener(v -> addPlayer());
    }
    
    private void setupValidation() {
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                validateAddPlayerForm();
            }
        };
        
        etJerseyNumber.addTextChangedListener(validationWatcher);
        etPlayerName.addTextChangedListener(validationWatcher);
    }
    
    private void validateAddPlayerForm() {
        String numberText = etJerseyNumber.getText().toString().trim();
        String name = etPlayerName.getText().toString().trim();
        
        // Reset validation state
        tvValidationMessage.setVisibility(View.GONE);
        btnAddPlayer.setEnabled(false);
        
        // Check if fields are empty
        if (numberText.isEmpty() || name.isEmpty()) {
            return; // Keep button disabled, no error message for empty fields
        }
        
        // Validate jersey number range
        try {
            int number = Integer.parseInt(numberText);
            if (number < 0 || number > 99) {
                showValidationError("Jersey number must be 0-99");
                return;
            }
            
            // Check for duplicate jersey number
            if (isJerseyNumberTaken(number, -1)) {
                showValidationError("Jersey number " + number + " is already taken");
                return;
            }
            
        } catch (NumberFormatException e) {
            showValidationError("Invalid jersey number");
            return;
        }
        
        // All validation passed
        btnAddPlayer.setEnabled(true);
    }
    
    private void showValidationError(String message) {
        tvValidationMessage.setText(message);
        tvValidationMessage.setVisibility(View.VISIBLE);
    }
    
    private boolean isJerseyNumberTaken(int number, int excludePlayerId) {
        for (TeamPlayer player : playersList) {
            if (player.getId() != excludePlayerId && player.getNumber() == number) {
                return true;
            }
        }
        return false;
    }
    
    private void addPlayer() {
        String numberText = etJerseyNumber.getText().toString().trim();
        String name = etPlayerName.getText().toString().trim();
        
        try {
            int number = Integer.parseInt(numberText);
            
            // Create new player
            int newPlayerId = getNextPlayerId();
            TeamPlayer newPlayer = new TeamPlayer(newPlayerId, team.getId(), number, name);
            
            // Add to list
            playersList.add(newPlayer);
            hasUnsavedChanges = true;
            
            // Refresh UI
            refreshPlayersList();
            
            // Clear form
            etJerseyNumber.setText("");
            etPlayerName.setText("");
            tvValidationMessage.setVisibility(View.GONE);
            
            Toast.makeText(context, "Added player: #" + number + " " + name, Toast.LENGTH_SHORT).show();
            
        } catch (NumberFormatException e) {
            showValidationError("Invalid jersey number");
        }
    }
    
    private int getNextPlayerId() {
        // Simple ID generation - find max existing ID and add 1
        int maxId = 0;
        for (TeamPlayer player : playersList) {
            if (player.getId() > maxId) {
                maxId = player.getId();
            }
        }
        return maxId + 1;
    }
    
    private void editPlayer(TeamPlayer player, int position) {
        // This will be handled by the adapter's inline editing
        playersAdapter.startEditing(position);
    }
    
    private void confirmDeletePlayer(TeamPlayer player, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Player")
               .setMessage("Remove " + player.toString() + " from " + team.getName() + "?")
               .setPositiveButton("Remove", (dialog, which) -> deletePlayer(player, position))
               .setNegativeButton("Cancel", null)
               .show();
    }
    
    private void deletePlayer(TeamPlayer player, int position) {
        // TODO: Check if player is used in scheduled games
        // For MVP, allow deletion
        
        playersList.remove(position);
        hasUnsavedChanges = true;
        refreshPlayersList();
        
        Toast.makeText(context, "Removed: " + player.toString(), Toast.LENGTH_SHORT).show();
    }
    
    private void refreshPlayersList() {
        // Update player count
        int playerCount = playersList.size();
        tvPlayerCount.setText(playerCount + " player" + (playerCount != 1 ? "s" : ""));
        
        // Show/hide empty state
        if (playerCount == 0) {
            lvPlayers.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            lvPlayers.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
        
        // Refresh adapter
        playersAdapter.notifyDataSetChanged();
    }
    
    private void handleCancel() {
        if (hasUnsavedChanges) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Discard Changes")
                   .setMessage("You have unsaved changes. Are you sure you want to cancel?")
                   .setPositiveButton("Discard", (d, w) -> dialog.dismiss())
                   .setNegativeButton("Keep Editing", null)
                   .show();
        } else {
            dialog.dismiss();
        }
    }
    
    private void handleSaveChanges() {
        // Update the team's player list
        team.getPlayers().clear();
        team.getPlayers().addAll(playersList);
        
        // Save to data provider
        team.save(DatabaseController.getInstance(context).getDatabaseHelper());
        
        // Notify listener
        if (listener != null) {
            listener.onPlayersChanged(team);
        }
        
        Toast.makeText(context, "✅ Player roster saved for " + team.getName(), Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }
    
    /**
     * Custom adapter for player management list
     */
    private class PlayerManagementAdapter extends BaseAdapter {
        private Context context;
        private List<TeamPlayer> players;
        private LayoutInflater inflater;
        private int editingPosition = -1;
        
        public PlayerManagementAdapter(Context context, List<TeamPlayer> players) {
            this.context = context;
            this.players = players;
            this.inflater = LayoutInflater.from(context);
        }
        
        @Override
        public int getCount() { return players.size(); }
        
        @Override
        public Object getItem(int position) { return players.get(position); }
        
        @Override
        public long getItemId(int position) { return players.get(position).getId(); }
        
        public void startEditing(int position) {
            editingPosition = position;
            notifyDataSetChanged();
        }
        
        public void stopEditing() {
            editingPosition = -1;
            notifyDataSetChanged();
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_player_management, parent, false);
            }
            
            TeamPlayer player = players.get(position);
            boolean isEditing = (position == editingPosition);
            
            // Get UI components
            LinearLayout layoutPlayerInfo = convertView.findViewById(R.id.layoutPlayerInfo);
            LinearLayout layoutEditForm = convertView.findViewById(R.id.layoutEditForm);
            LinearLayout layoutActionButtons = convertView.findViewById(R.id.layoutActionButtons);
            
            TextView tvPlayerNumber = convertView.findViewById(R.id.tvPlayerNumber);
            TextView tvPlayerName = convertView.findViewById(R.id.tvPlayerName);
            
            EditText etEditNumber = convertView.findViewById(R.id.etEditNumber);
            EditText etEditName = convertView.findViewById(R.id.etEditName);
            
            Button btnEditPlayer = convertView.findViewById(R.id.btnEditPlayer);
            Button btnDeletePlayer = convertView.findViewById(R.id.btnDeletePlayer);
            Button btnSaveEdit = convertView.findViewById(R.id.btnSaveEdit);
            Button btnCancelEdit = convertView.findViewById(R.id.btnCancelEdit);
            
            if (isEditing) {
                // Show edit form
                layoutPlayerInfo.setVisibility(View.GONE);
                layoutActionButtons.setVisibility(View.GONE);
                layoutEditForm.setVisibility(View.VISIBLE);
                
                // Populate edit form
                etEditNumber.setText(String.valueOf(player.getNumber()));
                etEditName.setText(player.getName());
                
                // Save edit
                btnSaveEdit.setOnClickListener(v -> {
                    String numberText = etEditNumber.getText().toString().trim();
                    String name = etEditName.getText().toString().trim();
                    
                    if (numberText.isEmpty() || name.isEmpty()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    try {
                        int number = Integer.parseInt(numberText);
                        if (number < 0 || number > 99) {
                            Toast.makeText(context, "Jersey number must be 0-99", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        if (isJerseyNumberTaken(number, player.getId())) {
                            Toast.makeText(context, "Jersey number " + number + " is already taken", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        // Update player
                        // Since TeamPlayer is immutable, we need to create a new one
                        TeamPlayer updatedPlayer = new TeamPlayer(player.getId(), player.getTeamId(), number, name);
                        players.set(position, updatedPlayer);
                        hasUnsavedChanges = true;
                        
                        stopEditing();
                        Toast.makeText(context, "Updated: #" + number + " " + name, Toast.LENGTH_SHORT).show();
                        
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Invalid jersey number", Toast.LENGTH_SHORT).show();
                    }
                });
                
                // Cancel edit
                btnCancelEdit.setOnClickListener(v -> stopEditing());
                
            } else {
                // Show player info
                layoutPlayerInfo.setVisibility(View.VISIBLE);
                layoutActionButtons.setVisibility(View.VISIBLE);
                layoutEditForm.setVisibility(View.GONE);
                
                // Display player info
                tvPlayerNumber.setText(String.valueOf(player.getNumber()));
                tvPlayerName.setText(player.getName());
                
                // Edit button
                btnEditPlayer.setOnClickListener(v -> startEditing(position));
                
                // Delete button
                btnDeletePlayer.setOnClickListener(v -> confirmDeletePlayer(player, position));
            }
            
            return convertView;
        }
    }
}
