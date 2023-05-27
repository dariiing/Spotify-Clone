--pentru importat din csv
CREATE OR REPLACE PROCEDURE import_song_data()
AS $$
BEGIN
  CREATE TEMPORARY TABLE temp_song_table (
    version INTEGER,
    id INTEGER,
    song_name VARCHAR(255),
    artist_name VARCHAR(255),
    album_name VARCHAR(255),
    genre VARCHAR(255)
  );

BEGIN
COPY temp_song_table (version, id, song_name, artist_name, album_name, genre)
    FROM 'C:\Users\daria\OneDrive\Desktop\spoticlone\src\main\resources\songs.csv'
    DELIMITER ',' CSV HEADER;
EXCEPTION
    WHEN OTHERS THEN
      RAISE EXCEPTION 'Error importing data from CSV: %', SQLERRM;
END;

INSERT INTO song_table (version, id, song_name, artist_name, album_name, genre)
SELECT version, id, song_name, artist_name, album_name, genre
FROM temp_song_table;

DROP TABLE temp_song_table;
END;
$$ LANGUAGE plpgsql;

--pentru importat userii
CREATE OR REPLACE PROCEDURE import_user_data()
AS $$
BEGIN
INSERT INTO application_user (version, id, username, name, hashed_password, profile_picture)
VALUES (1, '1', 'daria', 'Daria', '$2a$10$xdbKoM48VySZqVSU/cSlVeJn0Z04XCZ7KZBjUBC00eKo5uLswyOpe', NULL);

INSERT INTO application_user (version, id, username, name, hashed_password, profile_picture)
VALUES (1, '2', 'ana', 'Ana', '$2a$10$xdbKoM48VySZqVSU/cSlVeJn0Z04XCZ7KZBjUBC00eKo5uLswyOpe', NULL);
END;
$$ LANGUAGE plpgsql;

-- verifica genurile
CREATE OR REPLACE FUNCTION validate_genre()
  RETURNS TRIGGER AS $$
BEGIN
  IF NEW.genre NOT IN ('Rock', 'Pop', 'Grunge', 'Funk', 'R&B', 'Latin', 'Pop/Rock', 'Flamenco/Pop', 'Indie Rock', 'A cappella/Pop') THEN
    RAISE EXCEPTION 'Invalid genre: %', NEW.genre;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER validate_genre_trigger
    BEFORE INSERT ON song_table
    FOR EACH ROW
    EXECUTE FUNCTION validate_genre();

-- verifica melodii duplicate
CREATE OR REPLACE FUNCTION check_duplicate_song()
  RETURNS TRIGGER AS $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM song_table
    WHERE song_name = NEW.song_name
      AND artist_name = NEW.artist_name
      AND album_name = NEW.album_name
  ) THEN
    RAISE EXCEPTION 'Duplicate song entry: % - % - %', NEW.song_name, NEW.artist_name, NEW.album_name;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_duplicate_song_trigger
    BEFORE INSERT ON song_table
    FOR EACH ROW
    EXECUTE FUNCTION check_duplicate_song();

-- am folosit functia asta temporar pentru recomandari, dar nu prea are sens sa fie acolo
CREATE OR REPLACE FUNCTION add_recommendations()
RETURNS TRIGGER AS $$
BEGIN
    -- Generate a new id for the Recommendations entry
    NEW.id := nextval('idgenerator');

    -- Insert the record into Recommendations
    INSERT INTO Recommendations (id, version, user_id, song_name, artist_name, album_name, genre)
    VALUES (NEW.id, 1, NEW.user_id, NEW.song_name, NEW.artist_name, NEW.album_name, NEW.genre);

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- am incercat sa matchuiesc dupa genre si primesc erori
-- CREATE OR REPLACE FUNCTION add_recommendations()
-- RETURNS TRIGGER AS $$
-- DECLARE
-- majority_genre TEXT;
-- BEGIN
--     -- Get the majority genre among liked songs
-- SELECT genre
-- INTO majority_genre
-- FROM (
--          SELECT genre, COUNT(*) AS genre_count
--          FROM liked_songs
--          GROUP BY genre
--          ORDER BY genre_count DESC
--              LIMIT 1
--      ) AS subquery;
--
--
-- NEW.id := nextval('idgenerator');
--
--
--
-- INSERT INTO Recommendations (id, version, user_id, song_name, artist_name, album_name, genre)
-- SELECT NEW.id, 1, NEW.user_id, st.song_name, st.artist_name, st.album_name, st.genre
-- FROM song_table st
-- WHERE st.genre = majority_genre;
--
-- RETURN NULL;
-- END;
-- $$ LANGUAGE plpgsql;



CREATE TRIGGER add_recommendations_trigger
    AFTER INSERT ON liked_songs
    FOR EACH ROW
    EXECUTE FUNCTION add_recommendations();
