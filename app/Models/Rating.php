<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

/**
 * Modele Rating.
 * Note (1 a 5) et commentaire d un apprenant pour une formation.
 */
class Rating extends Model
{
    protected $fillable = [
        'user_id',
        'formation_id',
        'note',
        'commentaire',
    ];

    protected $casts = [
        'note' => 'integer',
    ];

    public function user()
    {
        return $this->belongsTo(User::class, 'user_id');
    }

    public function formation()
    {
        return $this->belongsTo(Formation::class, 'formation_id');
    }
}
