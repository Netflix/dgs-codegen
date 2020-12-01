FROM dockerregistry.test.netflix.net:7002/runtime/java:release

COPY build/distributions/*.tar /app/

RUN cd /app && \
    tar -xvf *.tar --strip-components=1 && \
    rm *.tar

WORKDIR /project

ENTRYPOINT ["/app/bin/graphql-dgs-codegen-core"]