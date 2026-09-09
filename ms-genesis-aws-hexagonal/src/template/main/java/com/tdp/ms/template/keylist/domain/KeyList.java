package com.tdp.ms.template.keylist.domain;

import java.util.Set;

public record KeyList(Integer id, String description, Set<KeyListDetail> details) {
}
