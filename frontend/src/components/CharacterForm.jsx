import React, { useState, useEffect } from 'react';

const CharacterForm = ({ character, onSave, onCancel }) => {
  const [form, setForm] = useState({
    // Base
    name: '',
    nickname: '',
    age: '',
    born: '',
    died: '',
    gender: 'male',

    // Physique
    height: '',
    weight: '',
    physical_description: '',
    distinctive_features: '',
    eye_color: '',
    hair_color: '',
    hair_style: '',
    build: 'average',

    // Psychologie
    personality: '',
    temperament: 'sanguine',
    mbti_type: '',
    strengths: '',
    weaknesses: '',
    fears: '',
    desires: '',

    // Historique
    history: '',
    backstory: '',
    character_arc: '',

    // Rôle
    role: 'supporting',
    archetype: 'ally',
    importance: 5,

    // Contexte
    occupation: '',
    social_class: '',
    education: '',
    family_background: '',

    // Listes
    skills: [],
    languages: [],
    hobbies: '',

    // Psychologie avancée
    core_belief: '',
    moral_alignment: 'true_neutral',
    internal_conflict: '',
    external_conflict: '',

    // Expression
    speech_pattern: '',
    catchphrase: '',
    accent: '',

    // Meta
    notes: '',
    tags: [],
  });

  const [currentTab, setCurrentTab] = useState('base');
  const [newSkill, setNewSkill] = useState('');
  const [newLanguage, setNewLanguage] = useState('');
  const [newTag, setNewTag] = useState('');
  const [validationErrors, setValidationErrors] = useState({});

  useEffect(() => {
    if (character) {
      setForm({ ...character });
    }
  }, [character]);

  const handleChange = (field, value) => {
    setForm({ ...form, [field]: value });
    // Effacer l'erreur de validation si l'utilisateur modifie le champ
    if (validationErrors[field]) {
      setValidationErrors({ ...validationErrors, [field]: null });
    }
  };

  const addSkill = () => {
    if (newSkill.trim() && !form.skills.includes(newSkill.trim())) {
      setForm({ ...form, skills: [...form.skills, newSkill.trim()] });
      setNewSkill('');
    }
  };

  const removeSkill = (skill) => {
    setForm({ ...form, skills: form.skills.filter(s => s !== skill) });
  };

  const addLanguage = () => {
    if (newLanguage.trim() && !form.languages.includes(newLanguage.trim())) {
      setForm({ ...form, languages: [...form.languages, newLanguage.trim()] });
      setNewLanguage('');
    }
  };

  const removeLanguage = (lang) => {
    setForm({ ...form, languages: form.languages.filter(l => l !== lang) });
  };

  const addTag = () => {
    if (newTag.trim() && !form.tags.includes(newTag.trim())) {
      setForm({ ...form, tags: [...form.tags, newTag.trim()] });
      setNewTag('');
    }
  };

  const removeTag = (tag) => {
    setForm({ ...form, tags: form.tags.filter(t => t !== tag) });
  };

  const validate = () => {
    const errors = {};

    if (!form.name || form.name.trim().length === 0) {
      errors.name = "Le nom est obligatoire";
    }

    if (form.age && (form.age < 0 || form.age > 200)) {
      errors.age = "L'âge doit être entre 0 et 200";
    }

    if (form.importance && (form.importance < 1 || form.importance > 10)) {
      errors.importance = "L'importance doit être entre 1 et 10";
    }

    if (form.mbti_type && !/^[IE][NS][TF][JP]$/.test(form.mbti_type)) {
      errors.mbti_type = "Format MBTI invalide (ex: INTJ)";
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validate()) {
      onSave(form);
    }
  };

  const tabs = [
    { id: 'base', label: 'Informations de base' },
    { id: 'physical', label: 'Apparence' },
    { id: 'psychology', label: 'Psychologie' },
    { id: 'history', label: 'Historique' },
    { id: 'role', label: 'Rôle narratif' },
    { id: 'context', label: 'Contexte social' },
    { id: 'expression', label: 'Expression' },
    { id: 'meta', label: 'Notes & Tags' },
  ];

  const renderTabContent = () => {
    switch (currentTab) {
      case 'base':
        return (
          <div className="tab-content">
            <h3>Informations de base</h3>

            <div className="form-row">
              <div className="form-field">
                <label>Nom complet *</label>
                <input
                  type="text"
                  value={form.name}
                  onChange={(e) => handleChange('name', e.target.value)}
                  className={validationErrors.name ? 'error' : ''}
                />
                {validationErrors.name && <span className="error-msg">{validationErrors.name}</span>}
              </div>

              <div className="form-field">
                <label>Surnom</label>
                <input
                  type="text"
                  value={form.nickname}
                  onChange={(e) => handleChange('nickname', e.target.value)}
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-field">
                <label>Âge</label>
                <input
                  type="number"
                  value={form.age}
                  onChange={(e) => handleChange('age', parseInt(e.target.value) || '')}
                  min="0"
                  max="200"
                  className={validationErrors.age ? 'error' : ''}
                />
                {validationErrors.age && <span className="error-msg">{validationErrors.age}</span>}
              </div>

              <div className="form-field">
                <label>Genre</label>
                <select value={form.gender} onChange={(e) => handleChange('gender', e.target.value)}>
                  <option value="male">Masculin</option>
                  <option value="female">Féminin</option>
                  <option value="non_binary">Non-binaire</option>
                  <option value="other">Autre</option>
                </select>
              </div>
            </div>

            <div className="form-row">
              <div className="form-field">
                <label>Date de naissance (YYYY-MM-DD)</label>
                <input
                  type="text"
                  value={form.born}
                  onChange={(e) => handleChange('born', e.target.value)}
                  placeholder="1990-01-15"
                />
              </div>

              <div className="form-field">
                <label>Date de mort (optionnel)</label>
                <input
                  type="text"
                  value={form.died}
                  onChange={(e) => handleChange('died', e.target.value)}
                  placeholder="2050-12-31"
                />
              </div>
            </div>
          </div>
        );

      case 'physical':
        return (
          <div className="tab-content">
            <h3>Apparence physique</h3>

            <div className="form-row">
              <div className="form-field">
                <label>Taille (cm)</label>
                <input
                  type="number"
                  value={form.height}
                  onChange={(e) => handleChange('height', parseInt(e.target.value) || '')}
                />
              </div>

              <div className="form-field">
                <label>Poids (kg)</label>
                <input
                  type="number"
                  value={form.weight}
                  onChange={(e) => handleChange('weight', parseInt(e.target.value) || '')}
                />
              </div>

              <div className="form-field">
                <label>Corpulence</label>
                <select value={form.build} onChange={(e) => handleChange('build', e.target.value)}>
                  <option value="slim">Mince</option>
                  <option value="average">Moyenne</option>
                  <option value="athletic">Athlétique</option>
                  <option value="muscular">Musclée</option>
                  <option value="heavy">Forte</option>
                  <option value="petite">Petite</option>
                </select>
              </div>
            </div>

            <div className="form-row">
              <div className="form-field">
                <label>Couleur des yeux</label>
                <input
                  type="text"
                  value={form.eye_color}
                  onChange={(e) => handleChange('eye_color', e.target.value)}
                />
              </div>

              <div className="form-field">
                <label>Couleur des cheveux</label>
                <input
                  type="text"
                  value={form.hair_color}
                  onChange={(e) => handleChange('hair_color', e.target.value)}
                />
              </div>

              <div className="form-field">
                <label>Coiffure</label>
                <input
                  type="text"
                  value={form.hair_style}
                  onChange={(e) => handleChange('hair_style', e.target.value)}
                />
              </div>
            </div>

            <div className="form-field">
              <label>Description physique générale</label>
              <textarea
                value={form.physical_description}
                onChange={(e) => handleChange('physical_description', e.target.value)}
                rows={4}
              />
            </div>

            <div className="form-field">
              <label>Signes distinctifs</label>
              <textarea
                value={form.distinctive_features}
                onChange={(e) => handleChange('distinctive_features', e.target.value)}
                rows={3}
                placeholder="Cicatrices, tatouages, particularités physiques..."
              />
            </div>
          </div>
        );

      case 'psychology':
        return (
          <div className="tab-content">
            <h3>Psychologie et caractère</h3>

            <div className="form-field">
              <label>Personnalité</label>
              <textarea
                value={form.personality}
                onChange={(e) => handleChange('personality', e.target.value)}
                rows={4}
                placeholder="Traits de personnalité principaux..."
              />
            </div>

            <div className="form-row">
              <div className="form-field">
                <label>Tempérament</label>
                <select value={form.temperament} onChange={(e) => handleChange('temperament', e.target.value)}>
                  <option value="sanguine">Sanguin</option>
                  <option value="choleric">Colérique</option>
                  <option value="melancholic">Mélancolique</option>
                  <option value="phlegmatic">Flegmatique</option>
                </select>
              </div>

              <div className="form-field">
                <label>Type MBTI (optionnel)</label>
                <input
                  type="text"
                  value={form.mbti_type}
                  onChange={(e) => handleChange('mbti_type', e.target.value.toUpperCase())}
                  maxLength={4}
                  placeholder="INTJ"
                  className={validationErrors.mbti_type ? 'error' : ''}
                />
                {validationErrors.mbti_type && <span className="error-msg">{validationErrors.mbti_type}</span>}
              </div>
            </div>

            <div className="form-field">
              <label>Forces</label>
              <textarea
                value={form.strengths}
                onChange={(e) => handleChange('strengths', e.target.value)}
                rows={3}
              />
            </div>

            <div className="form-field">
              <label>Faiblesses</label>
              <textarea
                value={form.weaknesses}
                onChange={(e) => handleChange('weaknesses', e.target.value)}
                rows={3}
              />
            </div>

            <div className="form-field">
              <label>Peurs</label>
              <textarea
                value={form.fears}
                onChange={(e) => handleChange('fears', e.target.value)}
                rows={3}
              />
            </div>

            <div className="form-field">
              <label>Désirs profonds</label>
              <textarea
                value={form.desires}
                onChange={(e) => handleChange('desires', e.target.value)}
                rows={3}
              />
            </div>

            <div className="form-field">
              <label>Croyance fondamentale</label>
              <input
                type="text"
                value={form.core_belief}
                onChange={(e) => handleChange('core_belief', e.target.value)}
                placeholder="Ex: La justice doit toujours triompher"
              />
            </div>

            <div className="form-field">
              <label>Alignement moral</label>
              <select value={form.moral_alignment} onChange={(e) => handleChange('moral_alignment', e.target.value)}>
                <option value="lawful_good">Loyal Bon</option>
                <option value="neutral_good">Neutre Bon</option>
                <option value="chaotic_good">Chaotique Bon</option>
                <option value="lawful_neutral">Loyal Neutre</option>
                <option value="true_neutral">Neutre</option>
                <option value="chaotic_neutral">Chaotique Neutre</option>
                <option value="lawful_evil">Loyal Mauvais</option>
                <option value="neutral_evil">Neutre Mauvais</option>
                <option value="chaotic_evil">Chaotique Mauvais</option>
              </select>
            </div>
          </div>
        );

      case 'history':
        return (
          <div className="tab-content">
            <h3>Historique et développement</h3>

            <div className="form-field">
              <label>Histoire / Passé</label>
              <textarea
                value={form.history}
                onChange={(e) => handleChange('history', e.target.value)}
                rows={4}
              />
            </div>

            <div className="form-field">
              <label>Backstory détaillé</label>
              <textarea
                value={form.backstory}
                onChange={(e) => handleChange('backstory', e.target.value)}
                rows={6}
                placeholder="Événements formatifs, origines, traumatismes..."
              />
            </div>

            <div className="form-field">
              <label>Arc narratif / Évolution</label>
              <textarea
                value={form.character_arc}
                onChange={(e) => handleChange('character_arc', e.target.value)}
                rows={5}
                placeholder="Comment le personnage évolue-t-il au cours de l'histoire ?"
              />
            </div>

            <div className="form-field">
              <label>Conflit intérieur</label>
              <textarea
                value={form.internal_conflict}
                onChange={(e) => handleChange('internal_conflict', e.target.value)}
                rows={3}
              />
            </div>

            <div className="form-field">
              <label>Conflit extérieur</label>
              <textarea
                value={form.external_conflict}
                onChange={(e) => handleChange('external_conflict', e.target.value)}
                rows={3}
              />
            </div>
          </div>
        );

      case 'role':
        return (
          <div className="tab-content">
            <h3>Rôle narratif</h3>

            <div className="form-row">
              <div className="form-field">
                <label>Rôle</label>
                <select value={form.role} onChange={(e) => handleChange('role', e.target.value)}>
                  <option value="protagonist">Protagoniste</option>
                  <option value="antagonist">Antagoniste</option>
                  <option value="supporting">Personnage secondaire</option>
                  <option value="minor">Personnage mineur</option>
                </select>
              </div>

              <div className="form-field">
                <label>Archétype</label>
                <select value={form.archetype} onChange={(e) => handleChange('archetype', e.target.value)}>
                  <option value="hero">Héros</option>
                  <option value="mentor">Mentor</option>
                  <option value="ally">Allié</option>
                  <option value="threshold_guardian">Gardien du seuil</option>
                  <option value="herald">Héraut</option>
                  <option value="shapeshifter">Métamorphe</option>
                  <option value="shadow">Ombre</option>
                  <option value="trickster">Tricheur</option>
                </select>
              </div>

              <div className="form-field">
                <label>Importance (1-10)</label>
                <input
                  type="range"
                  min="1"
                  max="10"
                  value={form.importance}
                  onChange={(e) => handleChange('importance', parseInt(e.target.value))}
                />
                <span className="range-value">{form.importance}</span>
              </div>
            </div>
          </div>
        );

      case 'context':
        return (
          <div className="tab-content">
            <h3>Contexte social</h3>

            <div className="form-field">
              <label>Occupation / Métier</label>
              <input
                type="text"
                value={form.occupation}
                onChange={(e) => handleChange('occupation', e.target.value)}
              />
            </div>

            <div className="form-row">
              <div className="form-field">
                <label>Classe sociale</label>
                <input
                  type="text"
                  value={form.social_class}
                  onChange={(e) => handleChange('social_class', e.target.value)}
                />
              </div>

              <div className="form-field">
                <label>Niveau d'éducation</label>
                <input
                  type="text"
                  value={form.education}
                  onChange={(e) => handleChange('education', e.target.value)}
                />
              </div>
            </div>

            <div className="form-field">
              <label>Contexte familial</label>
              <textarea
                value={form.family_background}
                onChange={(e) => handleChange('family_background', e.target.value)}
                rows={4}
              />
            </div>

            <div className="form-field">
              <label>Compétences</label>
              <div className="list-input">
                <input
                  type="text"
                  value={newSkill}
                  onChange={(e) => setNewSkill(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), addSkill())}
                  placeholder="Ajouter une compétence"
                />
                <button type="button" onClick={addSkill}>+</button>
              </div>
              <div className="tag-list">
                {form.skills.map((skill, idx) => (
                  <span key={idx} className="tag">
                    {skill}
                    <button type="button" onClick={() => removeSkill(skill)}>×</button>
                  </span>
                ))}
              </div>
            </div>

            <div className="form-field">
              <label>Langues parlées</label>
              <div className="list-input">
                <input
                  type="text"
                  value={newLanguage}
                  onChange={(e) => setNewLanguage(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), addLanguage())}
                  placeholder="Ajouter une langue"
                />
                <button type="button" onClick={addLanguage}>+</button>
              </div>
              <div className="tag-list">
                {form.languages.map((lang, idx) => (
                  <span key={idx} className="tag">
                    {lang}
                    <button type="button" onClick={() => removeLanguage(lang)}>×</button>
                  </span>
                ))}
              </div>
            </div>

            <div className="form-field">
              <label>Loisirs / Passions</label>
              <textarea
                value={form.hobbies}
                onChange={(e) => handleChange('hobbies', e.target.value)}
                rows={3}
              />
            </div>
          </div>
        );

      case 'expression':
        return (
          <div className="tab-content">
            <h3>Voix et expression</h3>

            <div className="form-field">
              <label>Façon de parler</label>
              <textarea
                value={form.speech_pattern}
                onChange={(e) => handleChange('speech_pattern', e.target.value)}
                rows={3}
                placeholder="Style de langage, vocabulaire, tics de langage..."
              />
            </div>

            <div className="form-row">
              <div className="form-field">
                <label>Phrase fétiche</label>
                <input
                  type="text"
                  value={form.catchphrase}
                  onChange={(e) => handleChange('catchphrase', e.target.value)}
                />
              </div>

              <div className="form-field">
                <label>Accent</label>
                <input
                  type="text"
                  value={form.accent}
                  onChange={(e) => handleChange('accent', e.target.value)}
                />
              </div>
            </div>
          </div>
        );

      case 'meta':
        return (
          <div className="tab-content">
            <h3>Notes et tags</h3>

            <div className="form-field">
              <label>Notes libres</label>
              <textarea
                value={form.notes}
                onChange={(e) => handleChange('notes', e.target.value)}
                rows={6}
                placeholder="Notes diverses, idées, références..."
              />
            </div>

            <div className="form-field">
              <label>Tags</label>
              <div className="list-input">
                <input
                  type="text"
                  value={newTag}
                  onChange={(e) => setNewTag(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), addTag())}
                  placeholder="Ajouter un tag"
                />
                <button type="button" onClick={addTag}>+</button>
              </div>
              <div className="tag-list">
                {form.tags.map((tag, idx) => (
                  <span key={idx} className="tag">
                    {tag}
                    <button type="button" onClick={() => removeTag(tag)}>×</button>
                  </span>
                ))}
              </div>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <div className="character-form">
      <div className="form-tabs">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            type="button"
            className={currentTab === tab.id ? 'active' : ''}
            onClick={() => setCurrentTab(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <form onSubmit={handleSubmit}>
        {renderTabContent()}

        <div className="form-actions">
          <button type="submit" className="primary">
            Enregistrer
          </button>
          <button type="button" onClick={onCancel}>
            Annuler
          </button>
        </div>
      </form>
    </div>
  );
};

export default CharacterForm;