package fpt.org.inblue.mapper;

import fpt.org.inblue.model.MentorReview;
import fpt.org.inblue.model.Post;
import fpt.org.inblue.model.dto.request.CreateMentorReviewRequest;
import fpt.org.inblue.model.dto.request.PostCreateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostMapper {
    @Mapping(target = "postId", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "major", ignore = true)
    Post toEntity(PostCreateRequest dto);
}
