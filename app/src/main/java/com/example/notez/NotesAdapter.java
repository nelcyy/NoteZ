package com.example.notez;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<NoteModel> notesList;
    private OnNoteClickListener listener;

    // Constructor to accept notes list and listener
    public NotesAdapter(List<NoteModel> notesList, OnNoteClickListener listener) {
        this.notesList = notesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout (item_note.xml)
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_note, parent, false);
        return new NoteViewHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        // Get the note at the current position
        NoteModel note = notesList.get(position);

        // Set the text for each view in the layout
        holder.title.setText(note.getTitle());
        holder.subtitle.setText(note.getSubtitle());
        holder.notes.setText(note.getFirstWord());
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public void updateData(List<NoteModel> newNotesList) {
        this.notesList = newNotesList; // Update the current notes list
        notifyDataSetChanged();       // Notify RecyclerView to refresh the UI
    }

    // ViewHolder class to hold references to the views
    public static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView title, subtitle, notes;

        public NoteViewHolder(@NonNull View itemView, OnNoteClickListener listener) {
            super(itemView);
            // Find views by ID
            title = itemView.findViewById(R.id.Title);
            subtitle = itemView.findViewById(R.id.Subtitle);
            notes = itemView.findViewById(R.id.Notes);

            // Handle item click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onNoteClick(position);  // Trigger click listener
                    }
                }
            });
        }
    }

    // Interface for note click handling
    public interface OnNoteClickListener {
        void onNoteClick(int position);
    }
}
