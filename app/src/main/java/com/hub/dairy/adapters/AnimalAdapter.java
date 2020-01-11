package com.hub.dairy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hub.dairy.R;
import com.hub.dairy.models.Animal;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AnimalAdapter extends RecyclerView.Adapter<AnimalAdapter.AnimalHolder> {

    private List<Animal> mAnimals;
    private AnimalClick mAnimalClick;

    public AnimalAdapter(List<Animal> animals, AnimalClick animalClick) {
        mAnimals = animals;
        mAnimalClick = animalClick;
    }

    @NonNull
    @Override
    public AnimalHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.animal_item, parent, false);
        return new AnimalHolder(view, mAnimalClick);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimalHolder holder, int position) {
        Animal animal = mAnimals.get(position);
        holder.bind(animal);
    }

    @Override
    public int getItemCount() {
        return mAnimals.size();
    }

    class AnimalHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView animalName, animalBreed;
        CircleImageView animalImage;
        AnimalClick mAnimalClick;

        AnimalHolder(@NonNull View itemView, AnimalClick animalClick) {
            super(itemView);
            mAnimalClick = animalClick;
            animalName = itemView.findViewById(R.id.animalName);
            animalBreed = itemView.findViewById(R.id.animalBreed);
            animalImage = itemView.findViewById(R.id.animalImage);
            itemView.setOnClickListener(this);
        }

        private void bind(Animal animal) {
            animalName.setText(animal.getName());
            animalBreed.setText(animal.getBreed());

            Glide.with(itemView.getContext())
                    .load(animal.getImageUrl())
                    .placeholder(R.drawable.ic_photo)
                    .error(R.drawable.ic_error_photo)
                    .into(animalImage);
        }

        @Override
        public void onClick(View v) {
            mAnimalClick.onAnimalClick(mAnimals.get(getAdapterPosition()));
        }
    }

    public interface AnimalClick{
        void onAnimalClick(Animal animal);
    }
}
