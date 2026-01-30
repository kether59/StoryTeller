"""
Module d'abstraction NLP - Compatible Python 3.14
Supporte spaCy 3.8+ et Stanza comme alternatives
"""
import logging
from typing import Optional, List, Dict, Any
from abc import ABC, abstractmethod

logger = logging.getLogger(__name__)


class Token:
    """Représentation unifiée d'un token"""
    def __init__(self, text: str, lemma: str, pos: str, dep: str):
        self.text = text
        self.lemma_ = lemma
        self.pos_ = pos
        self.dep_ = dep
        self.children = []
        self.sent = None


class Sentence:
    """Représentation unifiée d'une phrase"""
    def __init__(self, text: str, tokens: List[Token]):
        self.text = text
        self.tokens = tokens
        self.ents = []

        # Lier les tokens à la phrase
        for token in tokens:
            token.sent = self


class Entity:
    """Représentation unifiée d'une entité nommée"""
    def __init__(self, text: str, label: str, start_char: int, end_char: int):
        self.text = text
        self.label_ = label
        self.start_char = start_char
        self.end_char = end_char
        self.sent = None


class Document:
    """Représentation unifiée d'un document analysé"""
    def __init__(self, text: str, tokens: List[Token], sentences: List[Sentence], entities: List[Entity]):
        self.text = text
        self._tokens = tokens
        self._sentences = sentences
        self.ents = entities

        # Lier les entités aux phrases
        for ent in self.ents:
            for sent in self._sentences:
                if ent.text in sent.text:
                    ent.sent = sent
                    sent.ents.append(ent)
                    break

    def __iter__(self):
        """Permet d'itérer sur les tokens"""
        return iter(self._tokens)

    @property
    def sents(self):
        """Retourne les phrases"""
        return self._sentences


class NLPProvider(ABC):
    """Interface abstraite pour les fournisseurs NLP"""

    @abstractmethod
    def load(self, model_name: str) -> bool:
        """Charge le modèle NLP"""
        pass

    @abstractmethod
    def process(self, text: str) -> Document:
        """Analyse un texte et retourne un Document"""
        pass

    @abstractmethod
    def is_available(self) -> bool:
        """Vérifie si le provider est disponible"""
        pass


class SpacyProvider(NLPProvider):
    """Provider pour spaCy 3.8+ (compatible Python 3.14)"""

    def __init__(self):
        self.nlp = None
        self._available = False

    def load(self, model_name: str = "fr_core_news_md") -> bool:
        """Charge le modèle spaCy"""
        try:
            import spacy
            logger.info(f"Chargement du modèle spaCy: {model_name}")
            self.nlp = spacy.load(model_name)
            self._available = True
            logger.info(f"✓ Modèle spaCy chargé avec succès (version: {spacy.__version__})")
            return True
        except ImportError:
            logger.error("spaCy n'est pas installé. Installez-le avec: pip install spacy")
            return False
        except OSError as e:
            logger.error(f"Modèle spaCy '{model_name}' non trouvé. Téléchargez-le avec: python -m spacy download {model_name}")
            return False
        except Exception as e:
            logger.error(f"Erreur lors du chargement de spaCy: {e}")
            return False

    def process(self, text: str) -> Document:
        """Analyse un texte avec spaCy"""
        if not self.nlp:
            raise RuntimeError("Le modèle spaCy n'est pas chargé")

        doc = self.nlp(text)

        # Convertir les tokens
        tokens = []
        token_map = {}  # Pour mapper les tokens spaCy vers nos tokens

        for spacy_token in doc:
            token = Token(
                text=spacy_token.text,
                lemma=spacy_token.lemma_,
                pos=spacy_token.pos_,
                dep=spacy_token.dep_
            )
            tokens.append(token)
            token_map[spacy_token] = token

        # Reconstruire les relations parent-enfant
        for spacy_token in doc:
            our_token = token_map[spacy_token]
            our_token.children = [
                token_map[child]
                for child in spacy_token.children
                if child in token_map
            ]

        # Convertir les phrases
        sentences = []
        for sent in doc.sents:
            sent_tokens = [token_map[t] for t in sent if t in token_map]
            sentence = Sentence(text=sent.text, tokens=sent_tokens)
            sentences.append(sentence)

        # Convertir les entités
        entities = []
        for ent in doc.ents:
            entity = Entity(
                text=ent.text,
                label=ent.label_,
                start_char=ent.start_char,
                end_char=ent.end_char
            )
            entities.append(entity)

        return Document(text=text, tokens=tokens, sentences=sentences, entities=entities)

    def is_available(self) -> bool:
        return self._available


class StanzaProvider(NLPProvider):
    """Provider alternatif utilisant Stanza (Stanford NLP)"""

    def __init__(self):
        self.nlp = None
        self._available = False

    def load(self, model_name: str = "fr") -> bool:
        """Charge le modèle Stanza"""
        try:
            import stanza
            logger.info(f"Chargement du modèle Stanza: {model_name}")

            # Télécharger le modèle si nécessaire
            try:
                self.nlp = stanza.Pipeline(lang=model_name, processors='tokenize,pos,lemma,depparse,ner')
            except Exception:
                logger.info(f"Téléchargement du modèle Stanza '{model_name}'...")
                stanza.download(model_name)
                self.nlp = stanza.Pipeline(lang=model_name, processors='tokenize,pos,lemma,depparse,ner')

            self._available = True
            logger.info(f"✓ Modèle Stanza chargé avec succès")
            return True
        except ImportError:
            logger.error("Stanza n'est pas installé. Installez-le avec: pip install stanza")
            return False
        except Exception as e:
            logger.error(f"Erreur lors du chargement de Stanza: {e}")
            return False

    def process(self, text: str) -> Document:
        """Analyse un texte avec Stanza"""
        if not self.nlp:
            raise RuntimeError("Le modèle Stanza n'est pas chargé")

        doc = self.nlp(text)

        tokens = []
        sentences = []
        entities = []

        char_offset = 0

        for sent in doc.sentences:
            sent_tokens = []
            sent_start = char_offset

            for word in sent.words:
                token = Token(
                    text=word.text,
                    lemma=word.lemma,
                    pos=word.upos,
                    dep=word.deprel
                )
                tokens.append(token)
                sent_tokens.append(token)
                char_offset += len(word.text) + 1  # +1 pour l'espace

            sent_text = " ".join([w.text for w in sent.words])
            sentence = Sentence(text=sent_text, tokens=sent_tokens)
            sentences.append(sentence)

            # Entités pour cette phrase
            for ent in sent.ents:
                entity = Entity(
                    text=ent.text,
                    label=ent.type,
                    start_char=sent_start + ent.start_char,
                    end_char=sent_start + ent.end_char
                )
                entities.append(entity)

        return Document(text=text, tokens=tokens, sentences=sentences, entities=entities)

    def is_available(self) -> bool:
        return self._available


class NLPManager:
    """Gestionnaire de providers NLP avec fallback automatique"""

    def __init__(self):
        self.provider: Optional[NLPProvider] = None
        self.provider_name: Optional[str] = None

    def initialize(self, preferred: str = "spacy", model_name: Optional[str] = None) -> bool:
        """
        Initialise le provider NLP avec fallback automatique

        Args:
            preferred: "spacy" ou "stanza"
            model_name: Nom du modèle (optionnel)

        Returns:
            True si l'initialisation a réussi
        """
        providers_order = []

        if preferred == "spacy":
            providers_order = [
                (SpacyProvider(), model_name or "fr_core_news_md", "spaCy"),
                (StanzaProvider(), "fr", "Stanza")
            ]
        else:
            providers_order = [
                (StanzaProvider(), "fr", "Stanza"),
                (SpacyProvider(), model_name or "fr_core_news_md", "spaCy")
            ]

        for provider, model, name in providers_order:
            logger.info(f"Tentative d'initialisation avec {name}...")
            if provider.load(model):
                self.provider = provider
                self.provider_name = name
                logger.info(f"✓ NLP initialisé avec {name}")
                return True
            else:
                logger.warning(f"✗ Échec de l'initialisation avec {name}")

        logger.error("❌ Aucun provider NLP n'a pu être initialisé")
        return False

    def process(self, text: str) -> Optional[Document]:
        """Analyse un texte"""
        if not self.provider:
            logger.error("Aucun provider NLP n'est initialisé")
            return None

        try:
            return self.provider.process(text)
        except Exception as e:
            logger.error(f"Erreur lors de l'analyse NLP: {e}")
            return None

    def is_available(self) -> bool:
        """Vérifie si un provider est disponible"""
        return self.provider is not None and self.provider.is_available()

    def get_provider_info(self) -> Dict[str, Any]:
        """Retourne les informations sur le provider actuel"""
        return {
            "available": self.is_available(),
            "provider": self.provider_name,
            "status": "ready" if self.is_available() else "unavailable"
        }


# Instance globale du gestionnaire NLP
nlp_manager = NLPManager()