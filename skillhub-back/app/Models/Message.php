<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

/**
 * Modèle Message.
 * Stocké en MySQL dans la table messages.
 */
class Message extends Model
{
    /**
     * Champs autorisés.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'expediteur_id',
        'destinataire_id',
        'contenu',
        'lu',
    ];

    /**
     * Casts automatiques.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'lu' => 'boolean',
    ];

    /**
     * Relation : utilisateur expéditeur.
     */
    public function expediteur()
    {
        return $this->belongsTo(User::class, 'expediteur_id');
    }

    /**
     * Relation : utilisateur destinataire.
     */
    public function destinataire()
    {
        return $this->belongsTo(User::class, 'destinataire_id');
    }
}