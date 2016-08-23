
CREATE TABLE test_run(
    id int PRIMARY KEY AUTO_INCREMENT,
    revision VARCHAR(10),
    total INT,
    passed INT,
    failed INT,
    start_time timestamp,
    duration INT
);

CREATE TABLE test_history(
    test_run INT,
    name VARCHAR(250),
    test_state INT,
    PRIMARY KEY(test_run, name)
);