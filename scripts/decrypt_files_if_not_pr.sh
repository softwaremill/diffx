#!/usr/bin/env bash

if [[ "$TRAVIS_PULL_REQUEST" == "false" ]]; then
    openssl aes-256-cbc -K $encrypted_5331bd37a5e5_key -iv $encrypted_5331bd37a5e5_iv -in secrets.tar.enc -out secrets.tar -d
    tar xvf secrets.tar
fi
