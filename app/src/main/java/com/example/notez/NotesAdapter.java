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

    // Constructor to accept notes list
    public NotesAdapter(List<NoteModel> notesList) {
        this.notesList = notesList;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout (item_note.xml)
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_note, parent, false);
        return new NoteViewHolder(itemView);
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

    // ViewHolder class to hold references to the views
    public static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView title, subtitle, notes;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views by ID
            title = itemView.findViewById(R.id.Title);
            subtitle = itemView.findViewById(R.id.Subtitle);
            notes = itemView.findViewById(R.id.Notes);
        }
    }
}
