from flask import Blueprint

# This file is intentionally minimal; individual route modules register their blueprints.

# Helper import to make package importable
from . import story, characters, locations, timeline, ai